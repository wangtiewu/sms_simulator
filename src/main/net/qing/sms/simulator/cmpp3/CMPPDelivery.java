package net.qing.sms.simulator.cmpp3;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Properties;

import net.qing.sms.simulator.SmsSimulatorConfigure;
import eet.evar.base.DataFormatDeal;
import eet.evar.tool.DateFormat;
import io.netty.buffer.ByteBuf;

public class CMPPDelivery extends CMPPHeader {
	private final static int MAX_SEQ = 2 << 16;;
	private static volatile int seq = 0;
	private static int mscgId = Integer.parseInt(getProperties().getProperty(
			"cmpp.mscg.id"));

	byte[] msgId = new byte[8];// 8
	String destId;// 21
	String serviceId = "";// 10
	int tpPid;// 1
	int tpUdhi;// 1
	int msgFm;// 1
	String srcTerminalId;// 32，2.0版本为21
	int srcTerminalType=0;//1，3.0版本新增
	int registeredDelivery = 0;// 1
	int msgLength = 71;// 1
	String msgContent;
	String linkId="";//20，3.0版本新增
//	byte[] reserved = new byte[8];// 8，3.0版本已删除
	String submitTime = DateFormat.getNowByFormatString("MMddHHmmss");

	public CMPPDelivery() {
		this(false);
	}

	public CMPPDelivery(boolean isStatusReport) {
		super(CMPP_MESS_HEADER_LEN, CMPP_DELIVER, 0);
		if (isStatusReport) {
			registeredDelivery = 1;
		}
	}

	@Override
	public void decode(ByteBuf byteBuf) {
		// TODO Auto-generated method stub

	}

	@Override
	public void incode(ByteBuf byteBuf) {
		// TODO Auto-generated method stub
		createSeq();
		if (registeredDelivery == 1) {
			// 为状态报告
			setLen(12 + 8 + 21 + 10 + 1 + 1 + 1 + 32 + 1 + 1 + 1 + msgLength + 20);
			encodeHeader(byteBuf);
			byteBuf.writeBytes(msgId);
			byteBuf.writeBytes(destId.getBytes(), 0, 21);
			byteBuf.writeBytes(serviceId.getBytes(), 0, 10);
			byteBuf.writeByte(tpPid);
			byteBuf.writeByte(tpUdhi);
			byteBuf.writeByte(msgFm);
			byteBuf.writeBytes(srcTerminalId.getBytes(), 0, 32);
			byteBuf.writeByte(srcTerminalType);
			byteBuf.writeByte(registeredDelivery);
			byteBuf.writeByte(msgLength);
			byteBuf.writeBytes(msgId);// 8
			byteBuf.writeBytes("DELIVRD".getBytes(), 0, 7);// 7
			byteBuf.writeBytes(submitTime.getBytes(), 0, 10);// 10
			byteBuf.writeBytes(DateFormat.getNowByFormatString("MMddHHmmss")
					.getBytes(), 0, 10);// 10
			byteBuf.writeBytes(srcTerminalId.getBytes(), 0, 32);
			byteBuf.writeInt(0);
			byteBuf.writeBytes(linkId.getBytes(), 0, 20);
		} else {
			// 上行短信
			msgFm = 8;
			byte[] msgContentUnicode = null;
			try {
				msgContentUnicode = msgContent.getBytes("UTF-16be");
				msgLength = msgContentUnicode.length;
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			setLen(12 + 8 + 21 + 10 + 1 + 1 + 1 + 32 + 1 + 1 + 1 + msgLength + 20);
			encodeHeader(byteBuf);
			createMsgId();
			byteBuf.writeBytes(msgId);
			byteBuf.writeBytes(DataFormatDeal.strFillZero(destId, 21, DataFormatDeal.FILL_RIGHT).getBytes(), 0, 21);
			byteBuf.writeBytes(DataFormatDeal.strFillZero(serviceId, 10, DataFormatDeal.FILL_RIGHT).getBytes(), 0, 10);
			byteBuf.writeByte(tpPid);
			byteBuf.writeByte(tpUdhi);
			byteBuf.writeByte(msgFm);
			byteBuf.writeBytes(DataFormatDeal.strFillZero(srcTerminalId, 32, DataFormatDeal.FILL_RIGHT).getBytes(), 0, 32);
			byteBuf.writeByte(srcTerminalType);
			byteBuf.writeByte(registeredDelivery);
			byteBuf.writeByte(msgLength);
			byteBuf.writeBytes(msgContentUnicode);
			byteBuf.writeBytes(DataFormatDeal.strFillZero(linkId, 20, DataFormatDeal.FILL_RIGHT).getBytes(), 0, 20);
		}
	}

	public byte[] getMsgId() {
		return msgId;
	}

	public void setMsgId(byte[] msgId) {
		this.msgId = msgId;
	}

	public String getDestId() {
		return destId;
	}

	public void setDestId(String destId) {
		this.destId = destId;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
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

	public String getSrcTerminalId() {
		return srcTerminalId;
	}

	public void setSrcTerminalId(String srcTerminalId) {
		this.srcTerminalId = srcTerminalId;
	}

	public int getRegisteredDelivery() {
		return registeredDelivery;
	}

	public void setRegisteredDelivery(int registeredDelivery) {
		this.registeredDelivery = registeredDelivery;
	}

	public int getMsgLength() {
		return msgLength;
	}

	public void setMsgLength(int msgLength) {
		this.msgLength = msgLength;
	}

	public String getMsgContent() {
		return msgContent;
	}

	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}

	public int getSrcTerminalType() {
		return srcTerminalType;
	}

	public void setSrcTerminalType(int srcTerminalType) {
		this.srcTerminalType = srcTerminalType;
	}

	public String getLinkId() {
		return linkId;
	}

	public void setLinkId(String linkId) {
		this.linkId = linkId;
	}

	private void createMsgId() {
		StringBuffer msgIdSB = new StringBuffer();
		String time = DateFormat.getNowByFormatString("MMddHHmmss");
		byte[] bitsMsgId = new byte[64];
		byte mon = Byte.parseByte(time.substring(0, 2));
		// printBit(mon);
		DataFormatDeal.fillBinaryArray(bitsMsgId, mon, 0, 4);
		// printByte(bitsMsgId);
		byte day = Byte.parseByte(time.substring(2, 4));
		// printBit(day);
		DataFormatDeal.fillBinaryArray(bitsMsgId, day, 4, 5);
		// printByte(bitsMsgId);
		byte hour = Byte.parseByte(time.substring(4, 6));
		// printBit(hour);
		DataFormatDeal.fillBinaryArray(bitsMsgId, hour, 9, 5);
		// printByte(bitsMsgId);
		byte min = Byte.parseByte(time.substring(6, 8));
		// printBit(min);
		DataFormatDeal.fillBinaryArray(bitsMsgId, min, 14, 6);
		// printByte(bitsMsgId);
		byte sec = Byte.parseByte(time.substring(8, 10));
		// printBit(sec);
		DataFormatDeal.fillBinaryArray(bitsMsgId, sec, 20, 6);
		// printByte(bitsMsgId);
		byte[] mscgIdBytes = DataFormatDeal.intTobyte(mscgId, 3);
		DataFormatDeal.fillBinaryArray(bitsMsgId, mscgIdBytes[2], 26, 8);
		DataFormatDeal.fillBinaryArray(bitsMsgId, mscgIdBytes[1], 34, 8);
		DataFormatDeal.fillBinaryArray(bitsMsgId, mscgIdBytes[0], 42, 6);
		// printByte(bitsMsgId);
		byte[] seqBytes = DataFormatDeal.intTobyte(seq++, 2);
		DataFormatDeal.fillBinaryArray(bitsMsgId, seqBytes[0], 48, 8);
		DataFormatDeal.fillBinaryArray(bitsMsgId, seqBytes[1], 56, 8);
		StringBuffer strByte = new StringBuffer();
		for (int i = 0; i < 64; i++) {
			strByte.append(bitsMsgId[i]);
			if (i > 0 && (i + 1) % 8 == 0) {
				msgId[(i + 1) / 8 - 1] = DataFormatDeal.binaryString2Byte(strByte.toString());
				strByte.setLength(0);
			}
		}
		if (seq >= MAX_SEQ) {
			seq = 0;
		}
	}	

	private static Properties getProperties() {
		InputStream is = null;
		try {
			String configFile = "sms.properties";
			URL url = SmsSimulatorConfigure.class.getResource('/' + configFile);
			if (url == null) {
				System.out.println("配置文件不存在：" + configFile);
				return null;
			}
			try {
				is = new FileInputStream(URLDecoder.decode(url.getFile(),
						"UTF-8"));
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Properties properties = new Properties();
			try {
				properties.load(is);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return properties;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return null;
	}
}
