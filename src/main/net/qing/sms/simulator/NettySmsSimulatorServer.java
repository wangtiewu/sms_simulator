/**
 * Copyright 2012 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.qing.sms.simulator;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.qing.sms.simulator.cmpp.CMPPDecoder;
import eet.evar.tool.logger.Logger;
import eet.evar.tool.logger.LoggerFactory;

/**
 * Fully thread-safe.
 * 
 */
public class NettySmsSimulatorServer {
	private static final Logger log = LoggerFactory
			.getLogger(NettySmsSimulatorServer.class);
	private final Configuration configuration;
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	private final long serverStartTime = System.currentTimeMillis();// 服务器启动时间
	private final CancelableScheduler scheduler = new HashedWheelScheduler();

	public NettySmsSimulatorServer(Configuration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Get all clients connected to default namespace
	 * 
	 * @return clients collection
	 */

	public long getServerStartTime() {
		return serverStartTime;
	}

	/**
	 * Start server
	 */
	public void start() throws Exception {
		initGroups();
		Class channelClass = NioServerSocketChannel.class;
		if (configuration.isUseLinuxNativeEpoll()) {
			channelClass = EpollServerSocketChannel.class;
		}

		ServerBootstrap b = new ServerBootstrap();
		b.group(bossGroup, workerGroup)
				.channel(channelClass)
				.childHandler(
						new CMPPSimulatorChannelInitializer(serverStartTime, scheduler,
								configuration.getPingTimeout()));
		applyConnectionOptions(b);
		InetSocketAddress addr = new InetSocketAddress(configuration.getPort());
		if (configuration.getHostname() != null) {
			addr = new InetSocketAddress(configuration.getHostname(),
					configuration.getPort());
		}
		ChannelFuture f = b.bind(addr).sync();
		log.info("sms_simulator服务已启动：host:" + configuration.getHostname()
				+ ", port:" + configuration.getPort());
		f.channel().closeFuture().sync();
	}

	class CMPPSimulatorChannelInitializer extends
			ChannelInitializer<SocketChannel> {
		private ChannelHandler cmppSimulatorInboundHandler = null;

		public CMPPSimulatorChannelInitializer(long serverStartTime,
				CancelableScheduler scheduler, int pingTimeout) {
			cmppSimulatorInboundHandler = new CMPP2SimulatorHandler(
					serverStartTime, scheduler, configuration.getPingTimeout());
		}

		@Override
		public void initChannel(SocketChannel ch) throws Exception {
			ch.pipeline().addLast(
					"frameDecoder",
					new LengthFieldBasedFrameDecoder(1024 * 1024 * 1024, 0, 4, -4, 0));
			ch.pipeline().addLast("userDecoder", new CMPPDecoder());
			ch.pipeline().addLast(cmppSimulatorInboundHandler);
		}
	}

	protected void applyConnectionOptions(ServerBootstrap bootstrap) {
		SocketConfig config = configuration.getSocketConfig();
		bootstrap.childOption(ChannelOption.TCP_NODELAY, config.isTcpNoDelay());
		if (config.getTcpSendBufferSize() != -1) {
			bootstrap.childOption(ChannelOption.SO_SNDBUF,
					config.getTcpSendBufferSize());
		}
		if (config.getTcpReceiveBufferSize() != -1) {
			bootstrap.childOption(ChannelOption.SO_RCVBUF,
					config.getTcpReceiveBufferSize());
		}
		// bootstrap.option(ChannelOption.ALLOCATOR,
		// PooledByteBufAllocator.DEFAULT);
		bootstrap.option(ChannelOption.ALLOCATOR,
				PooledByteBufAllocator.DEFAULT);
		// bootstrap.childOption(ChannelOption.ALLOCATOR,
		// PooledByteBufAllocator.DEFAULT);
		bootstrap.childOption(ChannelOption.ALLOCATOR,
				PooledByteBufAllocator.DEFAULT);
		bootstrap.childOption(ChannelOption.SO_KEEPALIVE,
				config.isTcpKeepAlive());
		bootstrap.option(ChannelOption.SO_LINGER, config.getSoLinger());
		bootstrap.option(ChannelOption.SO_REUSEADDR, config.isReuseAddress());
		bootstrap.option(ChannelOption.SO_BACKLOG, config.getAcceptBackLog());
	}

	protected void initGroups() {
		if (configuration.isUseLinuxNativeEpoll()) {
			bossGroup = new EpollEventLoopGroup(configuration.getBossThreads());
			workerGroup = new EpollEventLoopGroup(
					configuration.getWorkerThreads());
		} else {
			bossGroup = new NioEventLoopGroup(configuration.getBossThreads());
			workerGroup = new NioEventLoopGroup(
					configuration.getWorkerThreads());
		}
	}

	/**
	 * Stop server
	 */
	public void stop() {
		bossGroup.shutdownGracefully().syncUninterruptibly();
		workerGroup.shutdownGracefully().syncUninterruptibly();
		scheduler.shutdown();
		log.info("sms_simulator服务已停止");
	}

	/**
	 * Allows to get configuration provided during server creation. Further
	 * changes on this object not affect server.
	 * 
	 * @return Configuration object
	 */
	public Configuration getConfiguration() {
		return configuration;
	}

	/**
	 * 获取调度器
	 * 
	 * @return
	 */
	public CancelableScheduler getScheduler() {
		return scheduler;
	}
}
