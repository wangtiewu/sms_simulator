package net.qing.sms.simulator;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import net.qing.sms.simulator.SchedulerKey.Type;
import net.qing.sms.simulator.cmpp.CMPPActiveTestResp;
import net.qing.sms.simulator.cmpp.CMPPConnect;
import net.qing.sms.simulator.cmpp.CMPPConnectResp;
import net.qing.sms.simulator.cmpp.CMPPDelivery;
import net.qing.sms.simulator.cmpp.CMPPHeader;
import net.qing.sms.simulator.cmpp.CMPPSubmit;
import net.qing.sms.simulator.cmpp.CMPPSubmitResp;
import net.qing.sms.simulator.cmpp.CMPPTerminateResp;
import net.qing.sms.simulator.cmpp.ErrorCode;
import net.qing.sms.simulator.cmpp.CMPPSendDelivery;
import eet.evar.StringDeal;
import eet.evar.base.DataFormatDeal;
import eet.evar.tool.MD5;
import eet.evar.tool.PseuRandom;
import eet.evar.tool.logger.Logger;
import eet.evar.tool.logger.LoggerFactory;
import eet.evar.tool.ratelimiting.TokenBucket;
import eet.evar.tool.ratelimiting.TokenBuckets;

@ChannelHandler.Sharable
public class CMPP2SimulatorHandler extends ChannelInboundHandlerAdapter {
	private static Logger logger = LoggerFactory
			.getLogger(CMPP2SimulatorHandler.class);
	public static final AttributeKey<String> ICP_ID = AttributeKey
			.<String> valueOf("ICP_ID");// 请求client对象
	public static final ConcurrentHashMap<String, CancelableScheduler> statusDeliverySchedulers = new ConcurrentHashMap<String, CancelableScheduler>();
	public static final ConcurrentHashMap<String, List<Channel>> connections = new ConcurrentHashMap<String, List<Channel>>();
	private static Queue<DeliveryReq> deliverQueue = new ConcurrentLinkedDeque<DeliveryReq>();
	private static AtomicInteger reportCount = new AtomicInteger();
	private static AtomicInteger reportSuccessCount = new AtomicInteger();
	Properties smsProperties = getProperties();
	TokenBucket submitTokenBucket = TokenBuckets
			.builder()
			.withCapacity(
					Integer.parseInt(smsProperties.getProperty(
							"cmpp.rate.limit", "30")))
			.withFixedIntervalRefillStrategy(
					Integer.parseInt(smsProperties.getProperty(
							"cmpp.rate.limit", "30")), 1, TimeUnit.SECONDS)
			.build();

	private static Properties getProperties() {
		InputStream is = null;
		try {
			String configFile = "sms.properties";
			URL url = SmsSimulatorConfigure.class.getResource('/' + configFile);
			if (url == null) {
				System.out.println("配置文件不存在：" + configFile);
				return null;
			}
			try {
				is = new FileInputStream(URLDecoder.decode(url.getFile(),
						"UTF-8"));
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Properties properties = new Properties();
			try {
				properties.load(is);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return properties;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public static void sendDelivery(String srcNumber, String destNumber,
			String content) {
		deliverQueue.add(new DeliveryReq(srcNumber, destNumber, content));
	}

	public CMPP2SimulatorHandler(long serverStartTime,
			CancelableScheduler scheduler, int pingTimeout) {
	}

	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		if (msg instanceof CMPPHeader) {
			if (logger.isInfoEnabled()) {
				logger.info("收到消息:" + JacksonUtils.beanToJson(msg));
			}
			CMPPHeader cmppHeader = (CMPPHeader) msg;
			switch (cmppHeader.getId()) {
			case CMPPHeader.CMPP_CONNECT:
				CMPPConnect cmppConnect = (CMPPConnect) msg;
				String remoteIp = getIp(ctx.channel().remoteAddress());
				String configedClientIp = smsProperties
						.getProperty("cmpp.client.ip");
				if (configedClientIp != null
						&& !configedClientIp.equals(remoteIp)) {
					CMPPConnectResp cmppConnectResp = new CMPPConnectResp(
							cmppHeader.getSeq());
					cmppConnectResp
							.setStatus(ErrorCode.ECODE_INVALID_REMOTE_IP);
					cmppConnectResp.setVersion(Integer.parseInt(smsProperties
							.getProperty("cmpp.version")));
					respMsg(false, ctx, cmppConnectResp);
					return;
				}
				String configedVersion = smsProperties
						.getProperty("cmpp.version");
				if (configedVersion != null
						&& Integer.parseInt(configedVersion) < cmppConnect
								.getVersion()) {
					CMPPConnectResp cmppConnectResp = new CMPPConnectResp(
							cmppHeader.getSeq());
					cmppConnectResp.setStatus(ErrorCode.ECODE_VERSION_HIGHTER);
					cmppConnectResp.setVersion(Integer.parseInt(smsProperties
							.getProperty("cmpp.version")));
					respMsg(false, ctx, cmppConnectResp);
					return;
				}
				String configedICPId = smsProperties.getProperty("cmpp.icp.id");
				String configedICPAuth = smsProperties
						.getProperty("cmpp.icp.auth");
				byte[] auth = new byte[128];
				DataFormatDeal.byteTobyte(auth, configedICPId.getBytes(), 1);
				DataFormatDeal.byteTobyte(auth, configedICPAuth.getBytes(),
						1 + configedICPId.getBytes().length + 9);
				byte[] timestamp = timestampToHexString(cmppConnect
						.getTimestamp());
				DataFormatDeal.byteTobyte(auth, timestamp,
						1 + configedICPId.getBytes().length + 9
								+ configedICPAuth.getBytes().length);
				MD5 md5 = new MD5();
				md5.Update(auth, 0, configedICPId.getBytes().length + 9
						+ configedICPAuth.getBytes().length + 10);
				byte[] dest = md5.Final();
				for (int i = 0; i < 16; i++) {
					if (dest[i] != cmppConnect.getAuth()[i]) {
						CMPPConnectResp cmppConnectResp = new CMPPConnectResp(
								cmppHeader.getSeq());
						cmppConnectResp.setStatus(ErrorCode.ECODE_AUTH_FAIL);
						cmppConnectResp.setVersion(Integer
								.parseInt(smsProperties
										.getProperty("cmpp.version")));
						respMsg(false, ctx, cmppConnectResp);
						return;
					}
				}
				int configedConnections = Integer.parseInt(smsProperties
						.getProperty("cmpp.client.connections", "1"));
				int curConnections = 0;
				List<Channel> channels = null;
				synchronized (connections) {
					channels = connections.get(configedICPId);
					if (channels != null) {
						curConnections = channels.size();
					}
					if (curConnections >= configedConnections) {
						CMPPConnectResp cmppConnectResp = new CMPPConnectResp(
								cmppHeader.getSeq());
						cmppConnectResp.setStatus(5);
						cmppConnectResp.setVersion(Integer
								.parseInt(smsProperties
										.getProperty("cmpp.version")));
						respMsg(false, ctx, cmppConnectResp);
						return;
					} else {
						if (channels == null) {
							channels = new ArrayList<Channel>();
							connections.put(configedICPId, channels);
						}
						channels.add(ctx.channel());
					}
				}
				CMPPConnectResp cmppConnectResp = new CMPPConnectResp(
						cmppHeader.getSeq());
				cmppConnectResp.setStatus(ErrorCode.ECODE_SUCCESS);
				md5 = new MD5();
				auth = new byte[128];
				DataFormatDeal
						.byteTobyte(auth, DataFormatDeal.intTobyte(
								ErrorCode.ECODE_SUCCESS, 1), 1);
				DataFormatDeal.byteTobyte(auth, cmppConnect.getAuth(), 2);
				DataFormatDeal.byteTobyte(auth, configedICPAuth.getBytes(),
						1 + 1 + 16);
				md5.Update(auth, 0, 1 + 16 + configedICPAuth.getBytes().length);
				byte[] respDest = md5.Final();
				cmppConnectResp.setAuth(respDest);
				cmppConnectResp.setVersion(cmppConnect.getVersion());
				respMsg(ctx, cmppConnectResp);
				ctx.channel().attr(ICP_ID).set(configedICPId);
				CancelableScheduler statusDeliveryScheduler = statusDeliverySchedulers
						.get(configedICPId);
				if (statusDeliveryScheduler == null) {
					synchronized (statusDeliverySchedulers) {
						if (statusDeliveryScheduler == null) {
							statusDeliveryScheduler = new HashedWheelScheduler();
							statusDeliverySchedulers.put(configedICPId,
									statusDeliveryScheduler);
						}
					}
				}
				break;
			case CMPPHeader.CMPP_ACTIVE_TEST:
				respMsg(ctx, new CMPPActiveTestResp(cmppHeader.getSeq()));
				break;
			case CMPPHeader.CMPP_SUBMIT:
				checkAndsendDelivery(ctx);
				CMPPSubmit submit = (CMPPSubmit) cmppHeader;
				if (submit.getMsgSrc() == null
						|| !submit.getMsgSrc().equals(
								smsProperties.getProperty("cmpp.icp.id"))) {
					CMPPSubmitResp resp = new CMPPSubmitResp(
							cmppHeader.getSeq());
					resp.setResult(7);
					respMsg(false, ctx, resp);
					return;
				}
				if (submit.getSrcId() == null
						|| !submit.getSrcId().startsWith(
								smsProperties.getProperty("cmpp.sp_no"))) {
					CMPPSubmitResp resp = new CMPPSubmitResp(
							cmppHeader.getSeq());
					resp.setResult(7);
					respMsg(false, ctx, resp);
					return;
				}
				if (!submitTokenBucket.tryConsume()) {
					logger.error("流量超过阀值");
					CMPPSubmitResp resp = new CMPPSubmitResp(
							cmppHeader.getSeq());
					resp.setResult(8);
					respMsg(false, ctx, resp);
					return;
				}
				CMPPSubmitResp submitResp = new CMPPSubmitResp(
						cmppHeader.getSeq());
				respMsg(ctx, submitResp);
				String lostReport = smsProperties
						.getProperty("cmpp.report.lost", "0");
				int iLostReport = Integer.valueOf(lostReport);
				if (submit.getRegisteredDelivery() == 1) {
					if (iLostReport > 0) {
						int lostIdx = 1000 / iLostReport;
						lostIdx = lostIdx <= 0 ? 1 : lostIdx;
						if (reportCount.incrementAndGet() % lostIdx == 0) {
							return;
						}
					}
					// 需要状态报告
					String icpId = ctx.channel().attr(ICP_ID).get();
					CancelableScheduler statusDeliveryScheduler1 = statusDeliverySchedulers
							.get(icpId);
					statusDeliveryScheduler1.schedule(
							new SchedulerKey(Type.STATUS_REPORT, ""
									+ cmppHeader.getSeq()),
							new CMPPSendDelivery(ctx, submitResp.getMsgId(),
									submit.getSrcId(), submit
											.getDestTerminalId(), submit
											.getServiceId()), (new PseuRandom(0)).random(2),
							TimeUnit.SECONDS);
				}
				break;
			case CMPPHeader.CMPP_DELIVER_RESP:
				if (reportSuccessCount.incrementAndGet() % 10000 == 0) {
					logger.error("reportSuccessCount:" + reportSuccessCount.incrementAndGet()
							+ ", seq:" + cmppHeader.getSeq());
				}
				String icpId = ctx.channel().attr(ICP_ID).get();
				CancelableScheduler statusDeliveryScheduler1 = statusDeliverySchedulers
						.get(icpId);
				statusDeliveryScheduler1.cancel(new SchedulerKey(
						Type.STATUS_REPORT, "" + cmppHeader.getSeq()));
				break;
			case CMPPHeader.CMPP_TERMINATE:
				CMPPTerminateResp cmppTerminateResp = new CMPPTerminateResp(
						cmppHeader.getSeq());
				respMsgAndClose(ctx, cmppTerminateResp);
				break;
			default:
				logger.error("未知的命令id：" + cmppHeader.getId());
				ctx.close();
				break;
			}
		} else {
			logger.warn("error object");
			ctx.close();
		}
	}

	private void checkAndsendDelivery(ChannelHandlerContext ctx) {
		// TODO Auto-generated method stub
		DeliveryReq deliveryReq = deliverQueue.poll();
		if (deliveryReq != null) {
			CMPPDelivery delivery = new CMPPDelivery(false);
			delivery.setDestId(deliveryReq.getDestNumber());
			delivery.setSrcTerminalId(deliveryReq.getSrcNumber());
			delivery.setMsgContent(deliveryReq.getContent());
			ByteBuf byteBuf = ctx.alloc().buffer();
			delivery.incode(byteBuf);
			ctx.writeAndFlush(byteBuf);
			if (logger.isInfoEnabled()) {
				logger.info("发送消息: " + JacksonUtils.beanToJson(delivery));
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		logger.error(MessageFormat.format("连接 {0} 发生异常，将被关闭", ctx.channel()
				.remoteAddress()), cause);
		ctx.close();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		String icpId = ctx.channel().attr(ICP_ID).get();
		if (icpId != null) {
			synchronized (connections) {
				List<Channel> channels = connections.get(icpId);
				if (channels != null) {
					if (channels.remove(ctx.channel())) {
						logger.error(MessageFormat.format("{0} 连接 {1} 已从连接表删除",
								icpId, ctx.channel().remoteAddress()));
					}
				} else {
					// CancelableScheduler statusDeliveryScheduler =
					// statusDeliverySchedulers
					// .remove(icpId);
					// if (statusDeliveryScheduler != null) {
					// statusDeliveryScheduler.shutdown();
					// statusDeliveryScheduler = null;
					// }
				}
			}
		}
		logger.error(MessageFormat.format("{0} 连接 {1} 已断开", icpId, ctx
				.channel().remoteAddress()));
	}

	private void respMsg(ChannelHandlerContext ctx, CMPPHeader respMsg) {
		respMsg(true, ctx, respMsg);
	}

	private void respMsgAndClose(ChannelHandlerContext ctx, CMPPHeader respMsg) {
		ByteBuf byteBuf = ctx.alloc().buffer();
		respMsg.incode(byteBuf);
		Future future = ctx.writeAndFlush(byteBuf);
		future.addListeners(ChannelFutureListener.CLOSE);
	}

	private void respMsg(boolean success, ChannelHandlerContext ctx,
			CMPPHeader respMsg) {
		ByteBuf byteBuf = ctx.alloc().buffer();
		respMsg.incode(byteBuf);
		ctx.writeAndFlush(byteBuf);
		if (success) {
			if (logger.isInfoEnabled()) {
				logger.info("响应消息{成功}: " + JacksonUtils.beanToJson(respMsg));
			}
		} else {
			if (logger.isInfoEnabled()) {
				logger.error("响应消息{失败}: " + JacksonUtils.beanToJson(respMsg));
			}
		}
	}

	private byte[] timestampToHexString(int timestamp) {
		char[] formatedTm = StringDeal.preFillZero("" + timestamp, 10)
				.toCharArray();
		byte[] hexTimestamp = new byte[10];
		for (int i = 0; i < 10; i++) {
			hexTimestamp[i] = (byte) formatedTm[i];
		}
		return hexTimestamp;
	}

	private String getIp(SocketAddress socketAddress) {
		if (socketAddress != null) {
			String ipPort = socketAddress.toString();
			if (ipPort.contains("/")) {
				ipPort = ipPort.substring(ipPort.indexOf("/") + 1);
			}
			if (ipPort.contains(":")) {
				return ipPort.substring(0, ipPort.indexOf(":"));
			}
			return ipPort;
		}
		return "";
	}

	private String getIpAndPort(SocketAddress socketAddress) {
		if (socketAddress != null) {
			String ipPort = socketAddress.toString();
			if (ipPort.contains("/")) {
				ipPort = ipPort.substring(ipPort.indexOf("/") + 1);
			}
			return ipPort;
		}
		return "";
	}
}

class DeliveryReq {
	private String srcNumber;
	private String destNumber;
	private String content;

	public DeliveryReq(String srcNumber, String destNumber, String content) {
		this.srcNumber = srcNumber;
		this.destNumber = destNumber;
		this.content = content;
	}

	public String getSrcNumber() {
		return srcNumber;
	}

	public void setSrcNumber(String srcNumber) {
		this.srcNumber = srcNumber;
	}

	public String getDestNumber() {
		return destNumber;
	}

	public void setDestNumber(String destNumber) {
		this.destNumber = destNumber;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
