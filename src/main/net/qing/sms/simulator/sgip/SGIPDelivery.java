package net.qing.sms.simulator.sgip;

import java.io.UnsupportedEncodingException;

import io.netty.buffer.ByteBuf;

public class SGIPDelivery extends SGIPHeader {
	private String userNumber;
	private String spNumber;
	private String content;
	
	public SGIPDelivery(byte[] seq) {
		super(SGIP_MESS_HEADER_LEN, SGIP_DELIVER, seq);
	}
	
	@Override
	public void decode(ByteBuf byteBuf) {
		// TODO Auto-generated method stub

	}

	@Override
	public void incode(ByteBuf byteBuf) {
		// TODO Auto-generated method stub
		int msgCoding = 8;
		int msgLength = 0;
		byte[] msgContentUnicode = null;
		try {
			msgContentUnicode = content.getBytes("UTF-16be");
			msgLength = msgContentUnicode.length;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.setLen(SGIP_MESS_HEADER_LEN+21+21+1+1+1+4+msgLength+8);
		encodeHeader(byteBuf);
		byteBuf.writeBytes(userNumber.getBytes(), 0 , 21);
		byteBuf.writeBytes(spNumber.getBytes(), 0, 21);
		byteBuf.writeByte(0);
		byteBuf.writeByte(0);
		byteBuf.writeByte(msgCoding);
		byteBuf.writeInt(msgLength);
		byteBuf.writeBytes(msgContentUnicode);
		byteBuf.writeBytes(new byte[8]);
	}

	public String getUserNumber() {
		return userNumber;
	}

	public void setUserNumber(String userNumber) {
		this.userNumber = userNumber;
	}

	public String getSpNumber() {
		return spNumber;
	}

	public void setSpNumber(String spNumber) {
		this.spNumber = spNumber;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
}
