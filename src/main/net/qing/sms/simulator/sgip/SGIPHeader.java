package net.qing.sms.simulator.sgip;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Properties;

import eet.evar.base.DataFormatDeal;
import eet.evar.tool.DateFormat;
import net.qing.sms.simulator.SmsSimulatorConfigure;
import io.netty.buffer.ByteBuf;

public abstract class SGIPHeader {
	private static final int MAX_SEQ = Integer.MAX_VALUE;

	public static final int SGIP_MESS_HEADER_LEN = 4+4+12;

	public static final int SGIP_BIND = 0x1;

	public static final int SGIP_BIND_RESP = 0x80000001;

	public static final int SGIP_UNBIND = 0x2;

	public static final int SGIP_UNBIND_RESP = 0x80000002;

	public static final int SGIP_SUBMIT = 0x3;

	public static final int SGIP_SUBMIT_RESP = 0x80000003;

	public static final int SGIP_DELIVER = 0x4;

	public static final int SGIP_DELIVER_RESP = 0x80000004;
	
	public static final int SGIP_REPORT = 0x5;

	public static final int SGIP_REPORT_RESP = 0x80000005;

	private static volatile int _seq = 0;
	
	private static int smgId = Integer.parseInt(getProperties().getProperty("sgip.smg.id"));

	private int len;
	private int id;
	private byte[] seq = new byte[12];
	
	public SGIPHeader(int id) {
		this.id = id;
	}
	
	public SGIPHeader(int len, int id) {
		this.len = len;
		this.id = id;
	}

	public SGIPHeader(int len, int id, byte[] seq) {
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

	public byte[] getSeq() {
		return seq;
	}

	public void setSeq(byte[] seq) {
		this.seq = seq;
	}

	public static synchronized byte[] createSeq() {
		byte[] curSeq = new byte[12];
		if (_seq >= MAX_SEQ) {
			_seq = 0;
		}
		DataFormatDeal.byteTobyte(curSeq, DataFormatDeal.intTobyte(smgId, 4), 1);
		DataFormatDeal.byteTobyte(curSeq, DataFormatDeal.intTobyte(Integer.parseInt(DateFormat.getNowByFormatString("MMddHHmmss")), 4), 5);
		DataFormatDeal.byteTobyte(curSeq, DataFormatDeal.intTobyte(_seq, 4), 9);
		_seq++;
		return curSeq;
	}

	protected void encodeHeader(ByteBuf byteBuf) {
		byteBuf.writeInt(getLen());
		byteBuf.writeInt(getId());
		byteBuf.writeBytes(getSeq());
	}

	public abstract void decode(ByteBuf byteBuf);

	public abstract void incode(ByteBuf byteBuf);
	
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
