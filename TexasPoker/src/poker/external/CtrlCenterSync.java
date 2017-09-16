package poker.external;

import poker.config.ConstantConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class CtrlCenterSync implements Runnable {
	public static final String CONTROL_CENTER_IP = ConstantConfig.getInstance().ccip;
	public static final int CONTROL_CENTER_PORT = ConstantConfig.getInstance().ccport;

	protected static CtrlCenterHandler ctrlCenterHandler;

	public static CtrlCenterHandler getChannel() {		
		return ctrlCenterHandler;
	}

	@Override
	public void run() {
		EventLoopGroup workerGroup = new NioEventLoopGroup(1);
		try {
			Bootstrap b = new Bootstrap();
			b.group(workerGroup);
			b.channel(NioSocketChannel.class);
			b.option(ChannelOption.SO_KEEPALIVE, true);
			b.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ctrlCenterHandler = new CtrlCenterHandler();
					ch.pipeline().addLast(ctrlCenterHandler);
				}
			});
			ChannelFuture f = b.connect(CONTROL_CENTER_IP, CONTROL_CENTER_PORT).sync();
			f.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			workerGroup.shutdownGracefully();
		}

	}

}
