package net.qing.sms.simulator.cmpp3;

import java.util.List;

import eet.evar.tool.logger.Logger;
import eet.evar.tool.logger.LoggerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

public class CMPP3Decoder extends MessageToMessageDecoder<ByteBuf> {
	private static Logger logger = LoggerFactory.getLogger(CMPP3Decoder.class);

	@Override
	protected void decode(ChannelHandlerContext arg0, ByteBuf arg1,
			List<Object> arg2) throws Exception {
		// TODO Auto-generated method stub
		int len = arg1.readInt();
		int id = arg1.readInt();
		int seq = arg1.readInt();

		switch (id) {
		case CMPPHeader.CMPP_CONNECT:
			CMPPConnect cmppConnect = new CMPPConnect(seq);
			cmppConnect.decode(arg1);
			arg2.add(cmppConnect);
			break;
		case CMPPHeader.CMPP_ACTIVE_TEST:
			CMPPActiveTest cmppActiveTest = new CMPPActiveTest(seq);
			cmppActiveTest.decode(arg1);
			arg2.add(cmppActiveTest);
			break;
		case CMPPHeader.CMPP_SUBMIT:
			CMPPSubmit cmppSubmit = new CMPPSubmit(len, seq);
			cmppSubmit.decode(arg1);
			arg2.add(cmppSubmit);
			break;
		case CMPPHeader.CMPP_DELIVER_RESP:
			CMPPDeliveryResp deliveryResp = new CMPPDeliveryResp(seq);
			deliveryResp.decode(arg1);
			arg2.add(deliveryResp);
			break;
		case CMPPHeader.CMPP_TERMINATE:
			CMPPTerminate cmppTerminate = new CMPPTerminate(seq);
			cmppTerminate.decode(arg1);
			arg2.add(cmppTerminate);
			break;
		default:
			logger.error("未知的命令id："+id);
		}
	}
}
