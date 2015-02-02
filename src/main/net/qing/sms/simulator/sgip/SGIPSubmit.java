package net.qing.sms.simulator.sgip;

import java.io.UnsupportedEncodingException;
import java.nio.Buffer;

import io.netty.buffer.ByteBuf;

public class SGIPSubmit extends SGIPHeader {
	String spNumber;//21
	String chargeNumber;//21
	int userCount;//1
	String userNumber;//21
	String corpId;//5
	String serviceType;//10
	int feeType;//1
	String feeValue;//6
	String givenValue;//6
	int agentFlag;//1
	int moRelateToMTFlag;//1
	int priority;//1
	String expireTime;//16
	String scheduleTime;//16
	int reportFlag;//1
	int tppid;//1
	int tpudhi;//1
	int messageCoding;//1
	int messageType;//1
	int messagelength;//4
	String messageContent;//
	String reserve;//8
	
	public SGIPSubmit(int len, byte[] seq) {
		// TODO Auto-generated constructor stub
		super(len, SGIPHeader.SGIP_SUBMIT, seq);
	}

	@Override
	public void decode(ByteBuf byteBuf) {
		// TODO Auto-generated method stub
		byte[] data = new byte[21];
		byteBuf.readBytes(data);
		spNumber = new String(data).trim();
		data = new byte[21];
		byteBuf.readBytes(data);
		chargeNumber = new String(data).trim();
		userCount = byteBuf.readUnsignedByte();
		for(int i=0;i<userCount; i++) {
			data = new byte[21];
			byteBuf.readBytes(data);
			userNumber = new String(data).trim();
		}
		data = new byte[5];
		byteBuf.readBytes(data);
		corpId = new String(data).trim();
		data = new byte[10];
		byteBuf.readBytes(data);
		serviceType = new String(data).trim();
		feeType = byteBuf.readUnsignedByte();
		data = new byte[6];
		byteBuf.readBytes(data);
		feeValue = new String(data).trim();
		data = new byte[6];
		byteBuf.readBytes(data);
		givenValue = new String(data).trim();
		agentFlag = byteBuf.readUnsignedByte();
		moRelateToMTFlag = byteBuf.readUnsignedByte();
		priority = byteBuf.readUnsignedByte();
		data = new byte[16];
		byteBuf.readBytes(data);
		expireTime = new String(data).trim();
		data = new byte[16];
		byteBuf.readBytes(data);
		scheduleTime = new String(data).trim();
		reportFlag = byteBuf.readUnsignedByte();
		tppid = byteBuf.readUnsignedByte();
		tpudhi = byteBuf.readUnsignedByte();
		messageCoding = byteBuf.readUnsignedByte();
		messageType = byteBuf.readUnsignedByte();
		messagelength = byteBuf.readInt();
		data = new byte[this.messagelength];
		byteBuf.readBytes(data, 0, this.messagelength);
		if (this.messageCoding == 8) {
			//UCS2编码
			try {
				this.messageContent = new String(data, "UTF-16be").trim();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(this.messageCoding == 15) {
			//GB
			try {
				this.messageContent = new String(data, "gbk").trim();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			this.messageContent = new String(data).trim();
		}
		data = new byte[8];
		byteBuf.readBytes(data);
		reserve = new String(data).trim();
	}

	@Override
	public void incode(ByteBuf byteBuf) {
		// TODO Auto-generated method stub

	}

	public String getSpNumber() {
		return spNumber;
	}

	public String getChargeNumber() {
		return chargeNumber;
	}

	public int getUserCount() {
		return userCount;
	}

	public String getUserNumber() {
		return userNumber;
	}

	public String getCorpId() {
		return corpId;
	}

	public String getServiceType() {
		return serviceType;
	}

	public int getFeeType() {
		return feeType;
	}

	public String getFeeValue() {
		return feeValue;
	}

	public String getGivenValue() {
		return givenValue;
	}

	public int getAgentFlag() {
		return agentFlag;
	}

	public int getMoRelateToMTFlag() {
		return moRelateToMTFlag;
	}

	public int getPriority() {
		return priority;
	}

	public String getExpireTime() {
		return expireTime;
	}

	public String getScheduleTime() {
		return scheduleTime;
	}

	public int getReportFlag() {
		return reportFlag;
	}

	public int getTppid() {
		return tppid;
	}

	public int getTpudhi() {
		return tpudhi;
	}

	public int getMessageCoding() {
		return messageCoding;
	}

	public int getMessageType() {
		return messageType;
	}

	public int getMessagelength() {
		return messagelength;
	}

	public String getMessageContent() {
		return messageContent;
	}

	public String getReserve() {
		return reserve;
	}
	
}
