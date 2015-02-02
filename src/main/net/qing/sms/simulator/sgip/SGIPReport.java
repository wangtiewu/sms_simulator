package net.qing.sms.simulator.sgip;

import io.netty.buffer.ByteBuf;

public class SGIPReport extends SGIPHeader {
	private byte[] submitSeq;//12
	private int reportType;//1
	private String userNumber;//21
	private int state;//1
	private int errorCode;//1
	private byte[] reserve = new byte[8];
	
	public SGIPReport(byte[] seq) {
		super(SGIP_MESS_HEADER_LEN + 12+1+21+1+1+8, SGIP_REPORT, seq);
		this.submitSeq = seq;
	}

	@Override
	public void decode(ByteBuf byteBuf) {
		// TODO Auto-generated method stub

	}

	@Override
	public void incode(ByteBuf byteBuf) {
		// TODO Auto-generated method stub
		encodeHeader(byteBuf);
		byteBuf.writeBytes(submitSeq);
		byteBuf.writeByte(reportType);
		byteBuf.writeBytes(userNumber.getBytes(), 0, 21);
		byteBuf.writeByte(state);
		byteBuf.writeByte(errorCode);
		byteBuf.writeBytes(reserve);
	}

	public void setReportType(int reportType) {
		this.reportType = reportType;
	}

	public void setUserNumber(String userNumber) {
		this.userNumber = userNumber;
	}

	public void setState(int state) {
		this.state = state;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
	
	
	
}
