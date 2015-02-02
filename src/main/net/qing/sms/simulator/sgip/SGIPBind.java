package net.qing.sms.simulator.sgip;

import io.netty.buffer.ByteBuf;

public class SGIPBind extends SGIPHeader {
	int loginType;
	String loginName;
	String loginPassword;
	String reserve;
	
	public  SGIPBind(byte[] seq) {
		// TODO Auto-generated constructor stub
		super(SGIP_MESS_HEADER_LEN + 41, SGIP_BIND, seq);
	}

	@Override
	public void decode(ByteBuf byteBuf) {
		// TODO Auto-generated method stub
		loginType = byteBuf.readByte();
		byte[] data = new byte[16];
		byteBuf.readBytes(data);
		loginName = new String(data).trim();
		data = new byte[16];
		byteBuf.readBytes(data);
		loginPassword = new String(data).trim();
		data = new byte[8];
		byteBuf.readBytes(data);
		reserve = new String(data);
	}

	@Override
	public void incode(ByteBuf byteBuf) {
		// TODO Auto-generated method stub
		encodeHeader(byteBuf);
		byteBuf.writeByte(loginType);
		byteBuf.writeBytes(loginName.getBytes(), 0, 16);
		byteBuf.writeBytes(loginPassword.getBytes(), 0, 16);
		if (reserve == null) {
			byteBuf.writeBytes(new byte[8]);
		}
		else {
			byteBuf.writeBytes(reserve.getBytes(), 0, 8);
		}
	}

	public int getLoginType() {
		return loginType;
	}

	public String getLoginName() {
		return loginName;
	}

	public String getLoginPassword() {
		return loginPassword;
	}

	public void setLoginType(int loginType) {
		this.loginType = loginType;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public void setLoginPassword(String loginPassword) {
		this.loginPassword = loginPassword;
	}
	

}
