package net.qing.sms.simulator.cmpp3;

import java.io.UnsupportedEncodingException;

import eet.evar.StringDeal;
import eet.evar.tool.Logger;
import io.netty.buffer.ByteBuf;

public class CMPPSubmit extends CMPPHeader {
	String msgId;//8
	int pkTotal;//1
	int pkNumber;//1
	int registeredDelivery;//1，0：不需要;1：需要
	int msgLevel;//1
	String serviceId;//10
	int feeUserType;//1
	String feeTerminalId;//32, 2.0 is 21
	int feeTerminalType;//1, 3.0 新增
	int tpPid;//1
	int tpUdhi;//1
	int msgFm;//1
	String msgSrc;//6
	String feeType;//2
	String feeCode;//6
	String validTime;//17
	String atTime;//17
	String srcId;//21
	int destUsrTl;//1
	String destTerminalId;//32*destUsrTl，2.0 is 21*destUsrTl
	int destTermialType;//1, 3.0新增字段
	int msgLenght;//1
	String msgContent;//，不定长
	String linkId;//20，3.0新增字段
//	String resrve;//8，3.0删除字段
	
	
	public CMPPSubmit(int len, int seq) {
		super(len, CMPP_SUBMIT, seq);
	}
	
	public CMPPSubmit(int seq) {
		super(CMPP_MESS_HEADER_LEN, CMPP_SUBMIT, seq);
	}

	@Override
	public void decode(ByteBuf byteBuf) {
		// TODO Auto-generated method stub
//		byte[] all = new byte[getLen() - 12];
//		byteBuf.readBytes(all, 0, getLen() - 12);
//		for(byte b : all) {
//			System.out.print(Integer.toHexString(b).length() < 2 ? "0"+Integer.toHexString(b):Integer.toHexString(b));
//			System.out.print(" ");
//		}
		byte[] data = new byte[8+1];
		byteBuf.readBytes(data, 0, 8);
		this.msgId = new String(data).trim();
		this.pkTotal = byteBuf.readByte();
		this.pkNumber = byteBuf.readByte();
		this.registeredDelivery = byteBuf.readByte();
		this.msgLevel = byteBuf.readByte();
		data = new byte[10+1];
		byteBuf.readBytes(data, 0, 10);
		this.serviceId = new String(data).trim();
		this.feeUserType = byteBuf.readByte();
		data = new byte[32+1];
		byteBuf.readBytes(data, 0, 32);
		this.feeTerminalId = new String(data).trim();
		this.feeTerminalType = byteBuf.readByte();
		this.tpPid = byteBuf.readByte();
		this.tpUdhi = byteBuf.readByte();
		this.msgFm = byteBuf.readByte();
		data = new byte[6+1];
		byteBuf.readBytes(data, 0, 6);
		this.msgSrc = new String(data).trim();
		data = new byte[2+1];
		byteBuf.readBytes(data, 0, 2);
		this.feeType = new String(data).trim();
		data = new byte[6+1];
		byteBuf.readBytes(data, 0, 6);
		this.feeCode = new String(data).trim();
		data = new byte[17+1];
		byteBuf.readBytes(data, 0, 17);
		this.validTime = new String(data).trim();
		data = new byte[17+1];
		byteBuf.readBytes(data, 0, 17);
		this.atTime = new String(data).trim();
		data = new byte[21+1];
		byteBuf.readBytes(data, 0, 21);
		this.srcId = new String(data).trim();
		this.destUsrTl = byteBuf.readByte();
		data = new byte[32+1];
		for(int i=0; i<this.destUsrTl; i++) {
			byteBuf.readBytes(data, 0, 32);
			this.destTerminalId = new String(data).trim();
			if (i > 1) {
				Logger.errorLog("接收号码多于1个，只处理最后一个号码");
			}
		}
		this.destTermialType = byteBuf.readUnsignedByte();
		this.msgLenght = byteBuf.readUnsignedByte();
		data = new byte[this.msgLenght+1];
		byteBuf.readBytes(data, 0, this.msgLenght);
		if (this.msgFm == 8) {
			//UCS2编码
			try {
				this.msgContent = new String(data, "UTF-16be").trim();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(this.msgFm == 15) {
			//GB
			try {
				this.msgContent = new String(data, "gbk").trim();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			this.msgContent = new String(data).trim();
		}
		data = new byte[20+1];
		byteBuf.readBytes(data, 0, 20);
		this.linkId = new String(data).trim();
	}

	@Override
	public void incode(ByteBuf byteBuf) {
		// TODO Auto-generated method stub
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public int getPkTotal() {
		return pkTotal;
	}

	public void setPkTotal(int pkTotal) {
		this.pkTotal = pkTotal;
	}

	public int getPkNumber() {
		return pkNumber;
	}

	public void setPkNumber(int pkNumber) {
		this.pkNumber = pkNumber;
	}

	public int getRegisteredDelivery() {
		return registeredDelivery;
	}

	public void setRegisteredDelivery(int registeredDelivery) {
		this.registeredDelivery = registeredDelivery;
	}

	public int getMsgLevel() {
		return msgLevel;
	}

	public void setMsgLevel(int msgLevel) {
		this.msgLevel = msgLevel;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public int getFeeUserType() {
		return feeUserType;
	}

	public void setFeeUserType(int feeUserType) {
		this.feeUserType = feeUserType;
	}

	public String getFeeTerminalId() {
		return feeTerminalId;
	}

	public void setFeeTerminalId(String feeTerminalId) {
		this.feeTerminalId = feeTerminalId;
	}

	public int getTpPid() {
		return tpPid;
	}

	public void setTpPid(int tpPid) {
		this.tpPid = tpPid;
	}

	public int getTpUdhi() {
		return tpUdhi;
	}

	public void setTpUdhi(int tpUdhi) {
		this.tpUdhi = tpUdhi;
	}

	public int getMsgFm() {
		return msgFm;
	}

	public void setMsgFm(int msgFm) {
		this.msgFm = msgFm;
	}

	public String getMsgSrc() {
		return msgSrc;
	}

	public void setMsgSrc(String msgSrc) {
		this.msgSrc = msgSrc;
	}

	public String getFeeType() {
		return feeType;
	}

	public void setFeeType(String feeType) {
		this.feeType = feeType;
	}

	public String getFeeCode() {
		return feeCode;
	}

	public void setFeeCode(String feeCode) {
		this.feeCode = feeCode;
	}

	public String getValidTime() {
		return validTime;
	}

	public void setValidTime(String validTime) {
		this.validTime = validTime;
	}

	public String getAtTime() {
		return atTime;
	}

	public void setAtTime(String atTime) {
		this.atTime = atTime;
	}

	public String getSrcId() {
		return srcId;
	}

	public void setSrcId(String srcId) {
		this.srcId = srcId;
	}

	public int getDestUsrTl() {
		return destUsrTl;
	}

	public void setDestUsrTl(int destUsrTl) {
		this.destUsrTl = destUsrTl;
	}

	public String getDestTerminalId() {
		return destTerminalId;
	}

	public void setDestTerminalId(String destTerminalId) {
		this.destTerminalId = destTerminalId;
	}

	public int getMsgLenght() {
		return msgLenght;
	}

	public void setMsgLenght(int msgLenght) {
		this.msgLenght = msgLenght;
	}

	public String getMsgContent() {
		return msgContent;
	}

	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}

	public int getFeeTerminalType() {
		return feeTerminalType;
	}

	public void setFeeTerminalType(int feeTerminalType) {
		this.feeTerminalType = feeTerminalType;
	}

	public String getLinkId() {
		return linkId;
	}

	public void setLinkId(String linkId) {
		this.linkId = linkId;
	}

	public int getDestTermialType() {
		return destTermialType;
	}

	public void setDestTermialType(int destTermialType) {
		this.destTermialType = destTermialType;
	}
	
}
