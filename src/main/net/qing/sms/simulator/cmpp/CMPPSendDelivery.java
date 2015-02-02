package net.qing.sms.simulator.cmpp;

import eet.evar.tool.logger.Logger;
import eet.evar.tool.logger.LoggerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.qing.sms.simulator.JacksonUtils;

public class CMPPSendDelivery implements Runnable {
	private static Logger logger = LoggerFactory
			.getLogger(CMPPSendDelivery.class);
	private byte[] msgId;
	private ChannelHandlerContext ctx;
	private String srcId;
	private String destId;
	private String serviceId;

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
		CMPPDelivery delivery = new CMPPDelivery(true);
		delivery.setMsgId(msgId);
		delivery.setDestId(srcId);
		delivery.setServiceId(serviceId);
		delivery.setSrcTerminalId(destId);
		ByteBuf byteBuf = ctx.alloc().buffer();
		delivery.incode(byteBuf);
		ctx.writeAndFlush(byteBuf);
		if (logger.isInfoEnabled()) {
			logger.info("发送消息: " + JacksonUtils.beanToJson(delivery));
		}
	}
}