package net.qing.sms.simulator.cmpp3;

import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import eet.evar.tool.IdGenerator;
import eet.evar.tool.logger.Logger;
import eet.evar.tool.logger.LoggerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.qing.sms.simulator.CMPP3SimulatorHandler;
import net.qing.sms.simulator.CancelableScheduler;
import net.qing.sms.simulator.JacksonUtils;
import net.qing.sms.simulator.SchedulerKey;
import net.qing.sms.simulator.SchedulerKey.Type;

public class CMPPSendDelivery implements Runnable {
	private static Logger logger = LoggerFactory
			.getLogger(CMPPSendDelivery.class);
	private byte[] msgId;
	private ChannelHandlerContext ctx;
	private String srcId;
	private String destId;
	private String serviceId;
	private static AtomicInteger count = new AtomicInteger();
	private static long t1 = System.currentTimeMillis();
	private int sendCount = 0;
	private int oldSeq = 0;

	public CMPPSendDelivery(ChannelHandlerContext ctx, byte[] msgId,
			String srcId, String destId, String serviceId) {
		this.ctx = ctx;
		this.msgId = msgId;
		this.srcId = srcId;
		this.destId = destId;
		this.serviceId = serviceId;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			Channel activeChannel = ctx.channel();
			String icpId = ctx.channel().attr(CMPP3SimulatorHandler.ICP_ID)
					.get();
			if (!activeChannel.isActive()) {
				activeChannel = getActiveChannel(icpId); 
				if (activeChannel == null) {
					CancelableScheduler statusDeliveryScheduler1 = CMPP3SimulatorHandler.statusDeliverySchedulers
							.get(icpId);
					sendCount ++;
					statusDeliveryScheduler1.schedule(new SchedulerKey(
							Type.STATUS_REPORT, "" + IdGenerator.createId()), this, 180,
							TimeUnit.SECONDS);
					return;
				}
			}
			CMPPDelivery delivery = new CMPPDelivery(true);
			delivery.setMsgId(msgId);
			delivery.setDestId(srcId);
			delivery.setServiceId(serviceId);
			delivery.setSrcTerminalId(destId);
			ByteBuf byteBuf = activeChannel.alloc().buffer();
			delivery.incode(byteBuf);
			int oSeq = oldSeq;
			oldSeq = delivery.getSeq();
			CancelableScheduler statusDeliveryScheduler1 = CMPP3SimulatorHandler.statusDeliverySchedulers
					.get(icpId);
			statusDeliveryScheduler1.schedule(new SchedulerKey(
					Type.STATUS_REPORT, "" + delivery.getSeq()), this, 180,
					TimeUnit.SECONDS);
			ByteBuf byteBuf1 = byteBuf.copy(0, 1);
			System.out.println(byteBuf1.writableBytes()+", "+byteBuf1.capacity()+", "+byteBuf1.readableBytes());
			activeChannel.writeAndFlush(byteBuf1);
			ByteBuf byteBuf2 = byteBuf.copy(1, 3);
			System.out.println(byteBuf2.writableBytes()+", "+byteBuf2.capacity()+", "+byteBuf2.readableBytes());
			activeChannel.writeAndFlush(byteBuf2);
			System.out.println(byteBuf.writableBytes()+", "+byteBuf.capacity()+", "+byteBuf.readableBytes());
			ByteBuf byteBuf3 = byteBuf.copy(4, 10);
			activeChannel.writeAndFlush(byteBuf3);
			ByteBuf byteBuf4 = byteBuf.copy(14, byteBuf.readableBytes()-10-4);
			activeChannel.writeAndFlush(byteBuf4);
			sendCount ++;
			int tc = count.incrementAndGet();
			if (tc % 1000 == 0) {
				logger.error("总数：" + tc + ", 每1000条耗时ms："
						+ (System.currentTimeMillis() - t1));
				t1 = System.currentTimeMillis();
			}
			if (sendCount > 1) {
				logger.error("resendDelivery: sendCount:"+sendCount+", seq:"+delivery.getSeq()+","+oSeq);
			}
//			logger.error("总数：" + tc +", sendCount:"+sendCount+", seq:"+delivery.getSeq()+","+oSeq);
			if (logger.isInfoEnabled()) {
				logger.info("发送消息: " + JacksonUtils.beanToJson(delivery));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Channel getActiveChannel(String icpId) {
		List<Channel> channels = CMPP3SimulatorHandler.connections.get(icpId);
		if (channels == null || channels.isEmpty()) {
			return null;
		}
		for(Channel channel : channels) {
			if (channel.isActive()) {
				return channel;
			}
		}
		return null;
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