package net.qing.sms.simulator.sgip;

import java.util.List;

import eet.evar.tool.logger.Logger;
import eet.evar.tool.logger.LoggerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

public class SGIPDecoder extends MessageToMessageDecoder<ByteBuf> {
	private static Logger logger = LoggerFactory.getLogger(SGIPDecoder.class);

	@Override
	protected void decode(ChannelHandlerContext arg0, ByteBuf arg1,
			List<Object> arg2) throws Exception {
		// TODO Auto-generated method stub
		int len = arg1.readInt();
		int id = arg1.readInt();
		byte seq[] = new byte[12];
		arg1.readBytes(seq);

		switch (id) {
		case SGIPHeader.SGIP_BIND:
			SGIPBind sgipBind = new SGIPBind(seq);
			sgipBind.decode(arg1);
			arg2.add(sgipBind);
			break;
		case SGIPHeader.SGIP_BIND_RESP:
			SGIPBindResp bindResp = new SGIPBindResp(seq);
			bindResp.decode(arg1);
			arg2.add(bindResp);
			break;
		case SGIPHeader.SGIP_SUBMIT:
			SGIPSubmit submit = new SGIPSubmit(len, seq);
			submit.decode(arg1);
			arg2.add(submit);
			break;
		case SGIPHeader.SGIP_REPORT_RESP:
			SGIPReportResp reportResp = new SGIPReportResp(seq);
			reportResp.decode(arg1);
			arg2.add(reportResp);
			break;
		case SGIPHeader.SGIP_DELIVER_RESP:
			SGIPDeliveryResp deliveryResp = new SGIPDeliveryResp(seq);
			deliveryResp.decode(arg1);
			arg2.add(deliveryResp);
			break;
		case SGIPHeader.SGIP_UNBIND:
			SGIPUnbind unbind = new SGIPUnbind(seq);
			unbind.decode(arg1);
			arg2.add(unbind);
			break;
		default:
			logger.error("未知的命令id："+id);
		}
	}
}
