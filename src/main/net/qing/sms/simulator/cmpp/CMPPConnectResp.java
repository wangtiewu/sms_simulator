package net.qing.sms.simulator.cmpp;

import io.netty.buffer.ByteBuf;

public class CMPPConnectResp extends CMPPHeader {
	private int status;
	private byte[] auth = new byte[16];
	private int version;

	public CMPPConnectResp(int seq) {
		super(CMPP_MESS_HEADER_LEN + 18, CMPP_CONNECT_RESP, seq);
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public byte[] getAuth() {
		return auth;
	}

	public void setAuth(byte[] auth) {
		this.auth = auth;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	@Override
	public void decode(ByteBuf byteBuf) {
		// TODO Auto-generated method stub
	}

	@Override
	public void incode(ByteBuf byteBuf) {
		// TODO Auto-generated method stub
		encodeHeader(byteBuf);
		byteBuf.writeByte(status);
		byteBuf.writeBytes(auth, 0, 16);
		byteBuf.writeByte(version);
	}

}
