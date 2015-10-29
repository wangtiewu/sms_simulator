package net.qing.sms.simulator.cmpp;

import io.netty.buffer.ByteBuf;

public class CMPPConnect extends CMPPHeader {
	private String sourceAddr;
	private byte[] auth = new byte[16];
	private int version;
	private int timestamp;
	
	public CMPPConnect(int seq) {
		super(CMPP_MESS_HEADER_LEN + 27, CMPP_CONNECT, seq);
	}

	public String getSourceAddr() {
		return sourceAddr;
	}

	public void setSourceAddr(String sourceAddr) {
		this.sourceAddr = sourceAddr;
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

	public int getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public void decode(ByteBuf byteBuf) {
		// TODO Auto-generated method stub
		byte[] data = new byte[6];
		byteBuf.readBytes(data, 0, 6);
		sourceAddr = new String(data);
		byteBuf.readBytes(auth, 0, 16);
		version = byteBuf.readByte();
		timestamp = byteBuf.readInt();
	}

	@Override
	public void incode(ByteBuf byteBuf) {
		// TODO Auto-generated method stub
		return;
	}
	
	
}
