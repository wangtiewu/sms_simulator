package net.qing.sms.simulator.sgip;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import eet.evar.base.DataFormatDeal;
import eet.evar.tool.logger.Logger;
import eet.evar.tool.logger.LoggerFactory;
import net.qing.sms.simulator.CancelableScheduler;
import net.qing.sms.simulator.HashedWheelScheduler;
import net.qing.sms.simulator.JacksonUtils;
import net.qing.sms.simulator.SGIPSimulatorHandler;
import net.qing.sms.simulator.SchedulerKey;
import net.qing.sms.simulator.SmsSimulatorConfigure;
import net.qing.sms.simulator.SchedulerKey.Type;
import net.qing.sms.simulator.cmpp.ErrorCode;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class SMGClient {
	private static final Logger logger = LoggerFactory
			.getLogger(SMGClient.class);
	private final ConcurrentHashMap<String, BlockingQueue<SGIPHeader>> responseMap = new ConcurrentHashMap<>();
	Properties smsProperties = getProperties();
	private EventLoopGroup workerGroup;
	private Channel channel;
	private volatile boolean closed = false;
	private boolean binded = false;

	public void connect() {
		workerGroup = new NioEventLoopGroup();
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(workerGroup).channel(NioSocketChannel.class)
				.option(ChannelOption.SO_KEEPALIVE, true)
				.option(ChannelOption.TCP_NODELAY, true)
				.remoteAddress(new InetSocketAddress(getSPHost(), getSPPort()))
				.handler(new SMGClientChannelInitializer());
		channel = bootstrap.connect().syncUninterruptibly().channel();
		logger.info("connect to server ok!");
		SGIPBind bind = new SGIPBind(SGIPHeader.createSeq());
		bind.setLoginType(2);
		bind.setLoginName(smsProperties.getProperty("sgip.loginname"));
		bind.setLoginPassword(smsProperties.getProperty("sgip.password"));
		SGIPBindResp bindResp = (SGIPBindResp)sendMsgSyn(channel, bind);
		if (bindResp.getResult() == 0) {
			logger.info("bind secuccess");
			binded = true;
		}
	}
	
	public void close() {
		closed = true;
		binded = false;
		if (null == channel) {
			return;
		}
		workerGroup.shutdownGracefully();
		channel.closeFuture().syncUninterruptibly();
		workerGroup = null;
		channel = null;
	}
	
	public boolean isBinded() {
		return binded;
	}
	
	private SGIPHeader sendMsgSyn(Channel channel, SGIPHeader msg) {
		ByteBuf byteBuf = channel.alloc().buffer();
		msg.incode(byteBuf);
		channel.writeAndFlush(byteBuf);
		responseMap.putIfAbsent(DataFormatDeal.bytes2BinaryString(msg.getSeq()), new LinkedBlockingQueue<SGIPHeader>(1));
		logger.info("发送消息: " + JacksonUtils.beanToJson(msg));
		SGIPHeader resp = getResponse(msg.getSeq());
		logger.info("收到消息: " + JacksonUtils.beanToJson(resp));
		return resp;
	}
	
	public void sendMsg(SGIPHeader msg) {
		if (closed) {
			return;
		}
		while(!binded) {
			connect();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		sendMsg(channel, msg);
	}
	
	private void sendMsg(Channel channel, SGIPHeader msg) {
		ByteBuf byteBuf = channel.alloc().buffer();
		msg.incode(byteBuf);
		channel.writeAndFlush(byteBuf);
		logger.info("发送消息: " + JacksonUtils.beanToJson(msg));
	}
	
	private SGIPHeader getResponse(final byte[] seq) {
		SGIPHeader result;
		responseMap.putIfAbsent(DataFormatDeal.bytes2BinaryString(seq), new LinkedBlockingQueue<SGIPHeader>(1));
		try {
			result = responseMap.get(DataFormatDeal.bytes2BinaryString(seq)).take();
		} catch (final InterruptedException ex) {
			return null;
		} finally {
			responseMap.remove(seq);
		}
		return result;
	}

	private void respMsg(boolean success, ChannelHandlerContext ctx,
			SGIPHeader respMsg) {
		ByteBuf byteBuf = ctx.alloc().buffer();
		respMsg.incode(byteBuf);
		ctx.writeAndFlush(byteBuf);
		if (success) {
			logger.info("响应消息{成功}: " + JacksonUtils.beanToJson(respMsg));
		} else {
			logger.error("响应消息{失败}: " + JacksonUtils.beanToJson(respMsg));
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

	private String getSPHost() {
		return "10.0.65.13";
	}

	private int getSPPort() {
		return 7777;
	}

	class SMGClientHandler extends ChannelInboundHandlerAdapter {
		public void channelRead(ChannelHandlerContext ctx, Object msg)
				throws Exception {
			if (msg instanceof SGIPHeader) {
				logger.info("收到消息:" + JacksonUtils.beanToJson(msg));
				SGIPHeader sgipHeader = (SGIPHeader) msg;
				switch (sgipHeader.getId()) {
				case SGIPHeader.SGIP_BIND_RESP:
					SGIPBindResp bindResp = (SGIPBindResp) sgipHeader;
					BlockingQueue<SGIPHeader> queue = responseMap.get(DataFormatDeal.bytes2BinaryString(sgipHeader.getSeq()));
					queue.add(bindResp);
					break;
				case SGIPHeader.SGIP_REPORT_RESP:
					break;
				case SGIPHeader.SGIP_DELIVER_RESP:
					break;
				default:
					logger.error("未知的命令id：" + sgipHeader.getId());
					ctx.close();
					break;
				}
			} else {
				logger.warn("error object");
				ctx.close();
			}
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
				throws Exception {
			logger.error(MessageFormat.format("连接 {0} 发生异常，将被关闭", ctx.channel()
					.remoteAddress()), cause);
			ctx.close();
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			String icpId = ctx.channel().attr(SGIPSimulatorHandler.ICP_ID)
					.get();
			logger.info(MessageFormat.format("{0} 连接 {1} 已断开", icpId, ctx
					.channel().remoteAddress()));
			binded = false;
			if (null == channel) {
				return;
			}
			workerGroup.shutdownGracefully();
			channel.closeFuture().syncUninterruptibly();
			workerGroup = null;
			channel = null;
		}
	}

	class SMGClientChannelInitializer extends ChannelInitializer<SocketChannel> {
		private ChannelHandler sgipSimulatorInboundHandler = null;

		public SMGClientChannelInitializer() {
			sgipSimulatorInboundHandler = new SMGClientHandler();
		}

		@Override
		public void initChannel(SocketChannel ch) throws Exception {
			ch.pipeline().addLast(
					"frameDecoder",
					new LengthFieldBasedFrameDecoder(1024 * 1024 * 1024, 0, 4,
							-4, 0));
			ch.pipeline().addLast("userDecoder", new SGIPDecoder());
			ch.pipeline().addLast(sgipSimulatorInboundHandler);
		}
	}
}
