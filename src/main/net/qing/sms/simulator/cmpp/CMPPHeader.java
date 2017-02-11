package net.qing.sms.simulator.cmpp;

import java.util.concurrent.atomic.AtomicInteger;

import io.netty.buffer.ByteBuf;

public abstract class CMPPHeader {
	private static final int MAX_SEQ = Integer.MAX_VALUE;

	public static final int CMPP_MESS_HEADER_LEN = 12;

	public static final int CMPP_CONNECT = 0x1;

	public static final int CMPP_CONNECT_RESP = 0x80000001;

	public static final int CMPP_TERMINATE = 0x2;

	public static final int CMPP_TERMINATE_RESP = 0x80000002;

	public static final int CMPP_SUBMIT = 0x4;

	public static final int CMPP_SUBMIT_RESP = 0x80000004;

	public static final int CMPP_DELIVER = 0x5;

	public static final int CMPP_DELIVER_RESP = 0x80000005;

	public static final int CMPP_ACTIVE_TEST = 0x8;

	public static final int CMPP_ACTIVE_TEST_RESP = 0x80000008;

	public static final int CMPP_FORWARD = 0x9;

	public static final int CMPP_FORWARD_RESP = 0x80000009;;
	public static final int CMPP_QUERY = 0x6;

	public static final int CMPP_QUERY_RESP = 0x80000006;

	public static final int CMPP_MT_ROUTE_UPDATE = 0x10;

	public static final int CMPP_MT_ROUTE_UPDATE_RESP = 0x80000010;

	public static final int CMPP_MO_ROUTE_UPDATE = 0x11;

	public static final int CMPP_MO_ROUTE_UPDATE_RESP = 0x80000011;
	
	private static  AtomicInteger _seq = new AtomicInteger(0);

	private int len;
	private int id;
	private int seq;

	public CMPPHeader(int len, int id, int seq) {
		this.len = len;
		this.id = id;
		this.seq = seq;
	}

	public int getLen() {
		return len;
	}

	public void setLen(int len) {
		this.len = len;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public int createSeq() {
		seq = _seq.incrementAndGet();
		return seq;
	}

	protected void encodeHeader(ByteBuf byteBuf) {
		byteBuf.writeInt(getLen());
		byteBuf.writeInt(getId());
		byteBuf.writeInt(getSeq());
	}

	public abstract void decode(ByteBuf byteBuf);

	public abstract void incode(ByteBuf byteBuf);
}
