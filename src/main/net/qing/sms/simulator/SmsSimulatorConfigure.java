package net.qing.sms.simulator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Properties;

import eet.evar.tool.Logger;

public class SmsSimulatorConfigure {
	private final static int DEFAULT_PING_INTERVAL = 30;//默认心跳间隔，30秒
	private static Properties properties = getProperties();
	
	/**
	 * 获取推送服务器监听IP地址
	 * @return
	 */
	public static String getCometServerIp() {
		String val = properties.getProperty("sms.server.ip");
		if (val == null) {
			return "0.0.0.0";
		}
		return val;
	}
	
	/**
	 * 获取推送服务监听端口
	 * @return
	 */
	public static int getCometServerPort() {
		String val = properties.getProperty("sms.server.port");
		if (val == null) {
			return 8080;
		}
		return Integer.parseInt(val);
	}
	
	private static Properties getProperties() {
		InputStream is = null;
		try {
			String configFile = "sms.properties";
			URL url = SmsSimulatorConfigure.class.getResource('/' + configFile);
			if (url == null) {
				System.out.println("配置文件不存在："+configFile);
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

	/**
	 * 心跳时间，单位秒，默认30秒
	 * @return
	 */
	public static int getPingInterval() {
		// TODO Auto-generated method stub
		String val = properties.getProperty("sms.ping.interval");
		if (val == null) {
			return DEFAULT_PING_INTERVAL;
		}
		return Integer.parseInt(val);
	}

	/**
	 * 心跳超时时间，单位秒，默认PingInterval*1.5
	 * @return
	 */
	public static int getPingTimeout() {
		// TODO Auto-generated method stub
		String val = properties.getProperty("sms.ping.timeout");
		if (val == null) {
			return (int)(DEFAULT_PING_INTERVAL*1.5);
		}
		return Integer.parseInt(val);
	}
}
