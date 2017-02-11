package net.qing.sms.simulator.cmpp3;

import io.netty.buffer.ByteBuf;

public class CMPPDeliveryResp extends CMPPHeader {
	private byte[] msgId = new byte[8];
	private int result = 0;//4，2.0版本为1
	

	public CMPPDeliveryResp(int seq) {
		super(CMPP_MESS_HEADER_LEN + 12, CMPP_DELIVER_RESP, seq);
	}
	
	@Override
	public void decode(ByteBuf byteBuf) {
		// TODO Auto-generated method stub
		byteBuf.readBytes(msgId);
		result = byteBuf.readInt();
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
