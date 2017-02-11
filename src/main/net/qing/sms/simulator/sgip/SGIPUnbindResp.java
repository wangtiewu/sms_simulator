package net.qing.sms.simulator.sgip;

import io.netty.buffer.ByteBuf;

public class SGIPUnbindResp extends SGIPHeader {
	public SGIPUnbindResp(byte[] seq) {
		super(SGIP_MESS_HEADER_LEN + 0, SGIP_UNBIND_RESP, seq);
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
