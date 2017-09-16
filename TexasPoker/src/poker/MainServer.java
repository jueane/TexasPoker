package poker;

import java.sql.SQLException;

import poker.config.ConstantConfig;
import poker.config.RoomConfig;
import poker.data.ConnectionPool;
import poker.external.CtrlCenterSync;
import poker.util.FileUtil;
import poker.util.JLog;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class MainServer {
	private static int port = 7000;

	public void launch() throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();

		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup);
			b.channel(NioServerSocketChannel.class);
			b.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast(new ChannelHandler());
				}
			});
			b.option(ChannelOption.SO_BACKLOG, 128);
			b.childOption(ChannelOption.SO_KEEPALIVE, true);

			ChannelFuture f = b.bind(port).sync();
			f.channel().closeFuture().sync();

		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}

	}

	public static void main(String[] args) {
		// 参数获取
		for (int i = 0; i < args.length; i++) {
			switch (args[i].toLowerCase()) {
			case "-port": {
				MainServer.port = Integer.parseInt(args[i++]);
				System.out.println("Bind port is " + args[i]);
				break;
			}
			case "-ccip": {
				ConstantConfig.getInstance().ccip = args[i++];
				System.out.println("CCIP is " + args[i]);
				break;
			}
			case "-ccport": {
				ConstantConfig.getInstance().ccport = Integer.parseInt(args[i++]);
				System.out.println("CCPort is " + args[i]);
				break;
			}
			case "-debug": {
				System.out.println("Enable debug.");
				JLog.debug = true;
				break;
			}
			case "-test": {
				System.out.println("Enable test.");
				JLog.test = true;
				break;
			}
			case "-logsize": {
				i++;
				FileUtil.filesize = Integer.parseInt(args[i++]);
				System.out.println("Log size set to " + FileUtil.filesize);
				break;
			}
			case "-version": {
				System.out.println("Poker server.Version 0.1.150105");
				return;
			}
			case "-help": {
				System.out.println("-port");
				System.out.print("                    ");
				System.out.println("setting port to bind");
				System.out.println("-ccip");
				System.out.print("                    ");
				System.out.println("setting ip of control center");
				System.out.println("-ccport");
				System.out.print("                    ");
				System.out.println("setting port of control center");
				System.out.println("-debug");
				System.out.print("                    ");
				System.out.println("enable debug mode");
				System.out.println("-test");
				System.out.print("                    ");
				System.out.println("enable test mode");
				System.out.println("-version");
				System.out.print("                    ");
				System.out.println("show version");
				return;
			}
			default:
				System.out.println("Parameters error.");
				return;
			}
		}

		// 启动
		System.out.println("Poker Server statup.");
		System.out.println("Initializing...");
		try {
			ConnectionPool.getConnection().close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		// 加载房间类型列表
		System.out.println("Load room type info.");
		RoomConfig.getInstance();

		// 启动房间检查器
		System.out.println("Load room checker.");
		RoomChecker.startup();

		// 启动中控同步线程
		System.out.println("Start CtrlCenter adapter.");
		new Thread(new CtrlCenterSync()).start();

		// 启动监视线程
		System.out.println("Start game monitor...");
		GameMonitor.startup();

		System.out.println("Initialization is complete.");

		try {
			new MainServer().launch();
			System.out.println("Done.");
		} catch (Exception e) {
			System.out.println("Error：" + e.getMessage());
		}

	}
}
