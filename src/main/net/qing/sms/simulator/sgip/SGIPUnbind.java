package net.qing.sms.simulator.sgip;

import io.netty.buffer.ByteBuf;

public class SGIPUnbind extends SGIPHeader {
	public SGIPUnbind(byte[] seq) {
		super(SGIP_MESS_HEADER_LEN + 0, SGIP_UNBIND, seq);
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
