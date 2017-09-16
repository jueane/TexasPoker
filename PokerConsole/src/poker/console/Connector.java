package poker.console;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class Connector {
	private String ip;
	private int port;

	public Connector(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	public void startup() throws Exception {
		EventLoopGroup workGroup = new NioEventLoopGroup(1);
		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(workGroup);
			bootstrap.channel(NioSocketChannel.class);
			bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
			bootstrap.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ClientHandler clientHandler = new ClientHandler();
					ch.pipeline().addLast(clientHandler);
				}
			});
			ChannelFuture f = bootstrap.connect(this.ip, this.port).sync();
			f.channel().closeFuture().sync();
		} finally {
			workGroup.shutdownGracefully();
		}
	}
}
