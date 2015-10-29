package net.qing.sms.simulator.cmpp;

import io.netty.buffer.ByteBuf;

public class CMPPTerminateResp extends CMPPHeader {
	public CMPPTerminateResp(int seq) {
		super(CMPP_MESS_HEADER_LEN + 0, CMPP_TERMINATE_RESP, seq);
	}
	
	@Override
	public void decode(ByteBuf byteBuf) {
		// TODO Auto-generated method stub

	}

	@Override
	public void incode(ByteBuf byteBuf) {
		// TODO Auto-generated method stub

	}

}
