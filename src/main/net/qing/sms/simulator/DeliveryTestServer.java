package net.qing.sms.simulator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Properties;

import eet.evar.StringDeal;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class DeliveryTestServer {
	private int port;

	public DeliveryTestServer(int port) {
		this.port = port;
	}

	public void run() throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap(); // (2)
			b.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class) // (3)
					.childHandler(new ChannelInitializer<SocketChannel>() { // (4)
								@Override
								public void initChannel(SocketChannel ch)
										throws Exception {
									ch.pipeline().addLast(
											new DeliveryTestServerHandler());
								}
							}).option(ChannelOption.SO_BACKLOG, 128) // (5)
					.childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

			// Bind and start to accept incoming connections.
			ChannelFuture f = b.bind(port).sync(); // (7)

			// Wait until the server socket is closed.
			// In this example, this does not happen, but you can do that to
			// gracefully
			// shut down your server.
			f.channel().closeFuture().sync();
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
		
	}
	

	public static void main(String[] args) throws Exception {
		int port;
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		} else {
			port = 9999;
		}
		new DeliveryTestServer(port).run();
	}
}

class DeliveryTestServerHandler extends ChannelInboundHandlerAdapter { // (1)

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
		// Discard the received data silently.
		ByteBuf bb = (ByteBuf)msg;
		byte[] line = new byte[bb.readableBytes()];
		((ByteBuf)msg).readBytes(line);
		String t="";
		try {
			t = new String(line, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String[] ta = StringDeal.split(t, " ");
		if (ta.length < 3) {
			ByteBuf byteBuf = ctx.alloc().buffer();
			try {
				byteBuf.writeBytes("use:srcNumber destNumber content\n".getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ctx.writeAndFlush(byteBuf);
			return;
		}
		String from = ta[0];
		String dest = ta[1];
		int idx = t.indexOf(dest);
		String content = t.substring(idx+dest.length()+1);
		String serverType = getProperties().getProperty("sms.server.type", "cmpp");
		if (serverType.equals("cmpp")) {
			CMPP2SimulatorHandler.sendDelivery(from, dest, content);
		}
		else if (serverType.equals("cmpp3")) {
			CMPP3SimulatorHandler.sendDelivery(from, dest, content);
		}
		else {
			SGIPSimulatorHandler.sendDelivery(from, dest, content);
		}
		ByteBuf byteBuf = ctx.alloc().buffer();
		byteBuf.writeBytes("ok!\n".getBytes());
		ctx.writeAndFlush(byteBuf);
		return;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
		// Close the connection when an exception is raised.
		cause.printStackTrace();
		ctx.close();
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
