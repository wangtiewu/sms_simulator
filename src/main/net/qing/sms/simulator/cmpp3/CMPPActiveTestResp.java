package net.qing.sms.simulator.cmpp3;

import io.netty.buffer.ByteBuf;

public class CMPPActiveTestResp extends CMPPHeader {
	private byte reserved = 0;
	
	public byte getReserved() {
		return reserved;
	}

	public void setReserved(byte reserved) {
		this.reserved = reserved;
	}

	public CMPPActiveTestResp(int seq) {
		super(CMPP_MESS_HEADER_LEN + 1, CMPP_ACTIVE_TEST_RESP, seq);
	}
	
	@Override
	public void decode(ByteBuf byteBuf) {
		// TODO Auto-generated method stub

	}

	@Override
	public void incode(ByteBuf byteBuf) {
		// TODO Auto-generated method stub
		encodeHeader(byteBuf);
		byteBuf.writeByte(reserved);
	}

}
