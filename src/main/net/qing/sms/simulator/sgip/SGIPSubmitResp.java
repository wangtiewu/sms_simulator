package net.qing.sms.simulator.sgip;

import io.netty.buffer.ByteBuf;

public class SGIPSubmitResp extends SGIPHeader {
	private int result;//1
	byte[] reserve = new byte[8];

	public SGIPSubmitResp(byte[] seq) {
		super(SGIP_MESS_HEADER_LEN + 9, SGIP_SUBMIT_RESP, seq);
	}
	
	@Override
	public void decode(ByteBuf byteBuf) {
		// TODO Auto-generated method stub

	}

	@Override
	public void incode(ByteBuf byteBuf) {
		// TODO Auto-generated method stub
		encodeHeader(byteBuf);
		byteBuf.writeByte(result);
		byteBuf.writeBytes(reserve);
	}

	public void setResult(int result) {
		this.result = result;
	}

	public int getResult() {
		return result;
	}
	
}
