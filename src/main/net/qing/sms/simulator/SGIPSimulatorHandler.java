package net.qing.sms.simulator;

import io.netty.buffer.ByteBuf;
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
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

import net.qing.sms.simulator.SchedulerKey.Type;
import net.qing.sms.simulator.cmpp.CMPPSendDelivery;
import net.qing.sms.simulator.cmpp.ErrorCode;
import net.qing.sms.simulator.sgip.SGIPBind;
import net.qing.sms.simulator.sgip.SGIPBindResp;
import net.qing.sms.simulator.sgip.SGIPHeader;
import net.qing.sms.simulator.sgip.SGIPSendDelivery;
import net.qing.sms.simulator.sgip.SGIPSendReport;
import net.qing.sms.simulator.sgip.SGIPSubmit;
import net.qing.sms.simulator.sgip.SGIPSubmitResp;
import net.qing.sms.simulator.sgip.SGIPUnbindResp;
import net.qing.sms.simulator.sgip.SMGClient;
import eet.evar.StringDeal;
import eet.evar.tool.logger.Logger;
import eet.evar.tool.logger.LoggerFactory;
import eet.evar.tool.ratelimiting.TokenBucket;
import eet.evar.tool.ratelimiting.TokenBuckets;

@ChannelHandler.Sharable
public class SGIPSimulatorHandler extends ChannelInboundHandlerAdapter {
	private static Logger logger = LoggerFactory
			.getLogger(SGIPSimulatorHandler.class);
	public static final AttributeKey<String> ICP_ID = AttributeKey
			.<String> valueOf("ICP_ID");// 请求client对象
	private final ConcurrentHashMap<String, CancelableScheduler> statusDeliverySchedulers = new ConcurrentHashMap<String, CancelableScheduler>();
	private final ConcurrentHashMap<String, Integer> connections = new ConcurrentHashMap<String, Integer>();
	private static Queue<SGIPDeliveryReq> deliverQueue = new ConcurrentLinkedDeque<SGIPDeliveryReq>();
	Properties smsProperties = getProperties();
	TokenBucket submitTokenBucket = TokenBuckets
			.builder()
			.withCapacity(
					Integer.parseInt(smsProperties.getProperty(
							"sgip.rate.limit", "30")))
			.withFixedIntervalRefillStrategy(
					Integer.parseInt(smsProperties.getProperty(
							"sgip.rate.limit", "30")), 1, TimeUnit.SECONDS)
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
		deliverQueue.add(new SGIPDeliveryReq(srcNumber, destNumber, content));
	}

	public SGIPSimulatorHandler(long serverStartTime,
			CancelableScheduler scheduler, int pingTimeout) {
	}

	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		if (msg instanceof SGIPHeader) {
			logger.info("收到消息:" + JacksonUtils.beanToJson(msg));
			SGIPHeader sgipHeader = (SGIPHeader) msg;
			switch (sgipHeader.getId()) {
			case SGIPHeader.SGIP_BIND:
				SGIPBind bind = (SGIPBind) msg;
				String remoteIp = getIp(ctx.channel().remoteAddress());
				String configedClientIp = smsProperties
						.getProperty("sgip.client.ip");
				if (configedClientIp != null
						&& !configedClientIp.equals(remoteIp)) {
					SGIPBindResp bindResp = new SGIPBindResp(
							sgipHeader.getSeq());
					bindResp.setResult(ErrorCode.ECODE_INVALID_REMOTE_IP);
					respMsg(false, ctx, bindResp);
					return;
				}
				String configedLoginName = smsProperties
						.getProperty("sgip.loginname");
				String configedPassword = smsProperties
						.getProperty("sgip.password");
				if (bind.getLoginName() == null
						|| !bind.getLoginName().equals(configedLoginName)
						|| bind.getLoginPassword() == null
						|| !bind.getLoginPassword().equals(configedPassword)) {
					SGIPBindResp bindResp = new SGIPBindResp(
							sgipHeader.getSeq());
					bindResp.setResult(ErrorCode.ECODE_AUTH_FAIL);
					respMsg(false, ctx, bindResp);
					return;
				}
				int configedConnections = Integer.parseInt(smsProperties
						.getProperty("sgip.client.connections", "1"));
				int curConnections = connections.get(configedLoginName) == null ? 0
						: connections.get(configedLoginName);
				if (curConnections >= configedConnections) {
					SGIPBindResp bindResp = new SGIPBindResp(
							sgipHeader.getSeq());
					bindResp.setResult(5);
					respMsg(false, ctx, bindResp);
					return;
				}
				SGIPBindResp bindResp = new SGIPBindResp(sgipHeader.getSeq());
				bindResp.setResult(ErrorCode.ECODE_SUCCESS);
				respMsg(ctx, bindResp);
				ctx.channel().attr(ICP_ID).set(configedLoginName);
				connections.put(configedLoginName, curConnections + 1);
				if (!statusDeliverySchedulers.containsKey(configedLoginName)) {
					CancelableScheduler statusDeliveryScheduler = new HashedWheelScheduler();
					statusDeliverySchedulers.put(configedLoginName,
							statusDeliveryScheduler);
				}
				break;
			case SGIPHeader.SGIP_UNBIND:
				SGIPUnbindResp unbindResp = new SGIPUnbindResp(sgipHeader.getSeq());
				respMsgAndClose(ctx, unbindResp);
				break;
			case SGIPHeader.SGIP_SUBMIT:
				checkAndsendDelivery(ctx);
				SGIPSubmit submit = (SGIPSubmit) sgipHeader;
				if (submit.getCorpId() == null
						|| !submit.getCorpId().equals(
								smsProperties.getProperty("sgip.corp.id"))) {
					SGIPSubmitResp resp = new SGIPSubmitResp(
							sgipHeader.getSeq());
					resp.setResult(7);
					respMsg(false, ctx, resp);
					return;
				}
				if (submit.getSpNumber() == null
						|| !submit.getSpNumber().startsWith(
								smsProperties.getProperty("sgip.sp_no"))) {
					SGIPSubmitResp resp = new SGIPSubmitResp(
							sgipHeader.getSeq());
					resp.setResult(7);
					respMsg(false, ctx, resp);
					return;
				}
				if (!submitTokenBucket.tryConsume()) {
					SGIPSubmitResp resp = new SGIPSubmitResp(
							sgipHeader.getSeq());
					resp.setResult(8);
					respMsg(false, ctx, resp);
					return;
				}
				SGIPSubmitResp resp = new SGIPSubmitResp(sgipHeader.getSeq());
				resp.setResult(0);
				respMsg(ctx, resp);
				if (submit.getReportFlag() == 1 || submit.getReportFlag() == 0) {
					// 需要状态报告
					CancelableScheduler statusDeliveryScheduler1 = statusDeliverySchedulers
							.get(ctx.channel().attr(ICP_ID).get());
					statusDeliveryScheduler1.schedule(
							new SchedulerKey(Type.STATUS_REPORT, ""
									+ sgipHeader.getSeq()),
							new SGIPSendReport(submit.getSeq(), submit
									.getSpNumber(), submit.getUserNumber(),
									submit.getServiceType(), 0), 5,
							TimeUnit.SECONDS);
				}
				break;
			default:
				logger.error("未知的命令id：" + sgipHeader.getId());
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
		SGIPDeliveryReq deliveryReq = deliverQueue.poll();
		if (deliveryReq != null) {
			CancelableScheduler statusDeliveryScheduler1 = statusDeliverySchedulers
					.get(ctx.channel().attr(ICP_ID).get());
			statusDeliveryScheduler1.schedule(
					new SchedulerKey(Type.DELIVERY, ""
							+ System.nanoTime()),
					new SGIPSendDelivery(deliveryReq.getSrcNumber(), deliveryReq.getDestNumber(), deliveryReq.getContent()), 1,
					TimeUnit.SECONDS);
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
			Integer connectionCount = connections.remove(icpId);
			if (connectionCount != null && connectionCount > 1) {
				connections.put(icpId, connectionCount - 1);
			}
		}
		logger.info(MessageFormat.format("{0} 连接 {1} 已断开", icpId, ctx.channel()
				.remoteAddress()));
	}

	private void respMsg(ChannelHandlerContext ctx, SGIPHeader respMsg) {
		respMsg(true, ctx, respMsg);
	}

	private void respMsgAndClose(ChannelHandlerContext ctx, SGIPHeader respMsg) {
		ByteBuf byteBuf = ctx.alloc().buffer();
		respMsg.incode(byteBuf);
		Future future = ctx.writeAndFlush(byteBuf);
		future.addListeners(ChannelFutureListener.CLOSE);
	}

	private void respMsg(boolean success, ChannelHandlerContext ctx,
			SGIPHeader respMsg) {
		ByteBuf byteBuf = ctx.alloc().buffer();
		respMsg.incode(byteBuf);
		ctx.writeAndFlush(byteBuf);
		if (success) {
			logger.info("响应消息{成功}: " + JacksonUtils.beanToJson(respMsg));
		} else {
			logger.error("响应消息{失败}: " + JacksonUtils.beanToJson(respMsg));
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

/**
 * 上行短信请求
 * 
 * @author Administrator
 *
 */
class SGIPDeliveryReq {
	private String srcNumber;
	private String destNumber;
	private String content;

	public SGIPDeliveryReq(String srcNumber, String destNumber, String content) {
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
