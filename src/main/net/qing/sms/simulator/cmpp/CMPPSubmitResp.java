package net.qing.sms.simulator.cmpp;

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

public class CMPPSubmitResp extends CMPPHeader {
	private final static int MAX_SEQ = 2 << 16;;
	private static volatile int seq = 0;
	private static int mscgId = Integer.parseInt(getProperties().getProperty("cmpp.mscg.id"));
	private byte[] msgId = new byte[8];// 8
	private int result=0;// 1

	public CMPPSubmitResp(int seq) {
		super(CMPP_MESS_HEADER_LEN + 9, CMPP_SUBMIT_RESP, seq);
		createMsgId();
	}

	private void createMsgId() {
		StringBuffer msgIdSB = new StringBuffer();
		String time = DateFormat.getNowByFormatString("MMddHHmmss");
		byte[] bitsMsgId = new byte[64];
		byte mon = Byte.parseByte(time.substring(0, 2));
		//printBit(mon);
		DataFormatDeal.fillBinaryArray(bitsMsgId, mon, 0, 4);
		//printByte(bitsMsgId);
		byte day = Byte.parseByte(time.substring(2, 4));
		//printBit(day);
		DataFormatDeal.fillBinaryArray(bitsMsgId, day, 4, 5);
		//printByte(bitsMsgId);
		byte hour = Byte.parseByte(time.substring(4, 6));
		//printBit(hour);
		DataFormatDeal.fillBinaryArray(bitsMsgId, hour, 9, 5);
		//printByte(bitsMsgId);
		byte min = Byte.parseByte(time.substring(6, 8));
		//printBit(min);
		DataFormatDeal.fillBinaryArray(bitsMsgId, min, 14, 6);
		//printByte(bitsMsgId);
		byte sec = Byte.parseByte(time.substring(8, 10));
		//printBit(sec);
		DataFormatDeal.fillBinaryArray(bitsMsgId, sec, 20, 6);
		//printByte(bitsMsgId);
		byte[] mscgIdBytes = DataFormatDeal.intTobyte(mscgId, 3);
		DataFormatDeal.fillBinaryArray(bitsMsgId, mscgIdBytes[2], 26, 8);
		DataFormatDeal.fillBinaryArray(bitsMsgId, mscgIdBytes[1], 34, 8);
		DataFormatDeal.fillBinaryArray(bitsMsgId, mscgIdBytes[0], 42, 6);
		//printByte(bitsMsgId);
		byte[] seqBytes = DataFormatDeal.intTobyte(seq++, 2);
		DataFormatDeal.fillBinaryArray(bitsMsgId, seqBytes[0], 48, 8);
		DataFormatDeal.fillBinaryArray(bitsMsgId, seqBytes[1], 56, 8);
		StringBuffer strByte = new StringBuffer();
		for(int i=0; i<64; i++) {
			strByte.append(bitsMsgId[i]);
			if (i>0 && (i+1) % 8 == 0) {
				msgId[(i+1)/8-1] = DataFormatDeal.binaryString2Byte(strByte.toString());
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

	@Override
	public void decode(ByteBuf byteBuf) {
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

	@Override
	public void incode(ByteBuf byteBuf) {
		// TODO Auto-generated method stub
		encodeHeader(byteBuf);
		byteBuf.writeBytes(msgId);
		byteBuf.writeByte(result);
	}

	public static void main(String[] args) {
		System.out.println((2<<16));
		for(int i=0; i<2<<16; i++) {
			CMPPSubmitResp submitResp = new CMPPSubmitResp(1);
			if (i % 10000 == 0) {
			}
		}
//		CMPPSubmitResp submitResp = new CMPPSubmitResp(1);
//		submitResp = new CMPPSubmitResp(1);
//		submitResp = new CMPPSubmitResp(1);
	}

}
