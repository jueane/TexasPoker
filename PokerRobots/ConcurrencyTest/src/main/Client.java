package main;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import entity.MemberInfo;

public class Client implements Runnable {
	private final String GAME_SERVER_IP;
	private static final int GAME_SERVER_PORT = 7000;

	private MemberInfo memberInfo;

	public Client(String ip, MemberInfo mem) {
		this.GAME_SERVER_IP = ip;
		this.memberInfo = mem;
	}

	@Override
	public void run() {
		try {
			startup();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void startup() throws Exception {
		EventLoopGroup workGroup = new NioEventLoopGroup(1);
		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(workGroup);
			bootstrap.channel(NioSocketChannel.class);
			bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
			bootstrap.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ClientEventHandler clientEventHandler = new ClientEventHandler();
					clientEventHandler.setMem(memberInfo);
					ch.pipeline().addLast(clientEventHandler);
				}
			});
			ChannelFuture f = bootstrap.connect(GAME_SERVER_IP, GAME_SERVER_PORT).sync();
			f.channel().closeFuture().sync();
		} finally {
			workGroup.shutdownGracefully();
		}
	}

}
