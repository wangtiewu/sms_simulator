package net.qing.sms.simulator.cmpp3;

import io.netty.buffer.ByteBuf;

public class CMPPTerminate extends CMPPHeader {
	public CMPPTerminate(int seq) {
		super(CMPP_MESS_HEADER_LEN + 0, CMPP_TERMINATE, seq);
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
