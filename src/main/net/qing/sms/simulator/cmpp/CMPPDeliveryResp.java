package net.qing.sms.simulator.cmpp;

import io.netty.buffer.ByteBuf;

public class CMPPDeliveryResp extends CMPPHeader {
	private byte[] msgId = new byte[8];
	private int result = 0;//1
	

	public CMPPDeliveryResp(int seq) {
		super(CMPP_MESS_HEADER_LEN + 9, CMPP_DELIVER_RESP, seq);
	}
	
	@Override
	public void decode(ByteBuf byteBuf) {
		// TODO Auto-generated method stub
		byteBuf.readBytes(msgId);
		result = byteBuf.readUnsignedByte();
	}

	@Override
	public void incode(ByteBuf byteBuf) {
		// TODO Auto-generated method stub
	}

	public byte[] getMsgId() {
		return msgId;
	}

	public void setMsgId(byte[] msgId) {
		this.msgId = msgId;
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	
}
