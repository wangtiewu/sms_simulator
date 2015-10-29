package net.qing.sms.simulator;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.util.concurrent.Executors;

import eet.evar.framework.bean.EvarBeanFactory;
import eet.evar.tool.filepkg.FileReadWrite;
import eet.evar.tool.logger.Logger;
import eet.evar.tool.logger.LoggerFactory;

/**
 * 启动类
 * 
 * @author eastelsoft
 *
 */
public class SmsSimulatorStartupX {
	static {
		LoggerFactory
				.setLoggerClassName("net.qing.sms.simulator.SmsSimulatorFailsaeLogger");
	}
	private final static Logger logger = LoggerFactory
			.getLogger(SmsSimulatorStartupX.class);
	
	public static volatile boolean THREAD_STATE = true;

	public void start() throws Exception {
		Configuration config = new Configuration();
		config.setHostname(SmsSimulatorConfigure.getCometServerIp());
		config.setPort(SmsSimulatorConfigure.getCometServerPort());
		config.setPingInterval(SmsSimulatorConfigure.getPingInterval());
		config.setPingTimeout(SmsSimulatorConfigure.getPingTimeout());
		SocketConfig socketConfig = new SocketConfig();
		socketConfig.setTcpSendBufferSize(32*1024);
		socketConfig.setTcpReceiveBufferSize(32*1024);
		config.setSocketConfig(socketConfig);
		final NettySmsSimulatorServer server = new NettySmsSimulatorServer(config);
		//启动Netty服务
		server.start();
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
//		EvarBeanFactory
//				.instance(new String[] { "spring-bean-container-cache.xml", "spring-bean-container-comet.xml" });
//		FileReadWrite.instance();
		eet.evar.tool.Logger.instance();
		new Thread() {
			public void run() {
				DeliveryTestServer deliveryTestServer = new DeliveryTestServer(9999);
				try {
					deliveryTestServer.run();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
		SmsSimulatorStartupX server = new SmsSimulatorStartupX();
		server.start();
	}

	static {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				if (logger.isInfoEnabled()) {
					logger.info("Run shutdown hook now.");
				}
				// server.stop();
				THREAD_STATE = false;
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, "SmsSimulatorShutdownHook"));
	}
}
