package main.robot.impl;

import java.util.Random;

import proto.ProtoFactory.ActionNoticeProto;
import proto.ProtoFactory.RaiseProto;
import proto.ProtoFactory.RoomInfoProto;

import com.google.protobuf.InvalidProtocolBufferException;

import main.ChannelHandler;
import main.GameRule;
import main.Pack;
import main.robot.Robot;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import entity.MemberInfo;

public class RobotImpl implements Robot {
	ChannelHandler handler;
	private final String GAME_SERVER_IP;
	private final int GAME_SERVER_PORT;

	EventLoopGroup workGroup;
	public MemberInfo mem;
	long checkTime;
	long lastBeginTime;
	boolean isRunning;
	Random random = new Random();
	byte[] holeCards;
	byte[] boardCards;
	boolean readyToAllin;

	int gameTimes = 0;

	int type = 1;
	int level = 1;

	public RobotImpl(String ip, int port, MemberInfo mem, int type, int level) {
		this.GAME_SERVER_IP = ip;
		this.GAME_SERVER_PORT = port;
		this.mem = mem;
		this.type = type;
		this.level = level;
		boardCards = new byte[5];
	}

	@Override
	public void action(byte[] buff) {
		ActionNoticeProto actionNoticeProto = null;
		try {
			actionNoticeProto = ActionNoticeProto.parseFrom(buff);
		} catch (InvalidProtocolBufferException e1) {
			e1.printStackTrace();
		}
		if (actionNoticeProto.getPlayerId() == mem.getId()) {
			long actDelay = random.nextInt(5) * 1000;// 出牌时间
			if (actDelay < 1000) {
				actDelay = 2000;
			}
			try {
				Thread.sleep(actDelay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			int act = 0;
			if (readyToAllin) {
				act = 4;
			} else {
				int randomInt = new Random().nextInt(100);
				if (actionNoticeProto.getMinRaise() <= actionNoticeProto.getRemainBankroll() && randomInt < 15) {
					act = 3;
				} else if (actionNoticeProto.getCheckable()) {
					act = 1;
				} else if (actionNoticeProto.getRemainBankroll() > 0) {
					if (actionNoticeProto.getMinCall() < actionNoticeProto.getRemainBankroll()) {
						act = 2;
					} else {
						// all in 或弃（150309改为只有弃)
						act = 5;
					}
				}
			}
			if (act <= 0 || act > 5) {
				act = 5;
			}
			switch (act) {
			case 1:
				this.handler.send(722031, null);
				break;
			case 2:
				this.handler.send(722032, null);
				break;
			case 3:
				int min = actionNoticeProto.getMinRaise();
				int max = actionNoticeProto.getRemainBankroll();
				Random rdm = new Random();
				int ante = rdm.nextInt(max);
				if (ante < min) {
					ante = min;
				}
				if (ante > 100000) {
					ante -= ante % 20000;
				} else if (ante > 10000) {
					ante -= ante % 2000;
				} else if (ante > 1000) {
					ante -= ante % 200;
				} else if (ante > 100) {
					ante -= ante % 20;
				} else if (ante > 10) {
					ante -= ante % 5;
				}
				RaiseProto.Builder raiseBuilder = RaiseProto.newBuilder();
				raiseBuilder.setAnte(ante);
				this.handler.send(722033, raiseBuilder.build().toByteArray());
				break;
			case 4:
				this.handler.send(722034, null);
				break;
			case 5:
				this.handler.send(722035, null);
				break;
			default:
				System.out.println("Send nothing.");
				break;
			}
		}

	}

	private void robotStart() {
		workGroup = new NioEventLoopGroup(1);
		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(workGroup);
			bootstrap.channel(NioSocketChannel.class);
			bootstrap.option(ChannelOption.SO_KEEPALIVE, true);

			this.handler = new ChannelHandler();
			this.handler.robot = this;

			bootstrap.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast(handler);
				}
			});
			ChannelFuture f = bootstrap.connect(GAME_SERVER_IP, GAME_SERVER_PORT).sync();
			f.channel().closeFuture().sync();
		} catch (Exception e) {
		}
	}

	@Override
	public void start() {
		start(0);
	}

	private void start(final long delay) {
		System.out.println("Robot " + mem.getId() + " 启动...");
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (delay > 0) {
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				isRunning = true;
				robotStart();
			}
		}).start();
		lastBeginTime = System.currentTimeMillis();
	}

	@Override
	public void restart() {
		System.out.println("Robot " + mem.getId() + " Stop.");
		handler.send(721090, null);
		isRunning = false;
		workGroup.shutdownGracefully();
		start(10000);
	}

	@Override
	public MemberInfo getUsernfo() {
		return mem;
	}

	@Override
	public void setUserinfo(MemberInfo mem) {
		this.mem = mem;
	}

	@Override
	public void setTime(long time) {
		this.checkTime = time;

	}

	@Override
	public long getTime() {
		return this.checkTime;
	}

	@Override
	public void gameEnd() {
		this.gameTimes++;
		if (gameTimes >= 10) {
			if (random.nextInt(100) <= 30) {
				System.out.println("Robot " + mem.getId() + " " + mem.getUsername() + " will leave...");
				restart();
			}
			this.gameTimes = 0;
		}

		// 重置公牌
		for (int i = 0; i < boardCards.length; i++) {
			boardCards[i] = 0;
		}
	}

	@Override
	public void setHoleCards(byte[] cards) {
		this.holeCards = cards;
		if ((holeCards[0] & 0xf) + (holeCards[1] & 0xf) >= 0x1b) {
			readyToAllin = true;
		} else if ((holeCards[0] & 0xf) == (holeCards[1] & 0xf) && (holeCards[0] & 0xf) >= 0xa) {
			readyToAllin = true;
		} else {
			readyToAllin = false;
		}

		if (readyToAllin) {
			System.out.println("Robot " + mem.getId() + " holecards were " + (holeCards[0] & 0xf) + "," + (holeCards[1] & 0xf) + "，all in.");
		}
	}

	@Override
	public void broadcastCards(byte[] cards) {
		// 判断是否观众
		if (this.holeCards == null) {
			return;
		}
		int i = 0;

		while (i < 4 && boardCards[i] != 0) {
			i++;
		}
		for (int j = 0; j < cards.length; j++) {
			boardCards[i + j] = cards[j];
		}
		// 第i张为空，即已发i张牌
		if (i + cards.length == 5) {
			byte[] combineCards = new byte[7];
			for (int k = 0; k < boardCards.length; k++) {
				combineCards[i] = boardCards[i];
			}
			combineCards[5] = this.holeCards[0];
			combineCards[6] = this.holeCards[1];
			GameRule gr = new GameRule();
			gr.add(mem.getId(), combineCards);
			int cardsType = gr.scoreList.get(0).type;
			System.out.println("Robot " + mem.getId() + " 牌型：" + gr.scoreList.get(0).title);
			if (cardsType >= 5) {
				System.out.println("Robot " + mem.getId() + " all in.");
				readyToAllin = true;
			}
		}
	}

	@Override
	public void packHandler(Pack pack) {
		switch (pack.code) {
		case 711035: {
			RoomInfoProto roomInfo = null;
			try {
				roomInfo = RoomInfoProto.parseFrom(pack.data);
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
			if (roomInfo.hasDealtBoardCards()) {
				for (int i = 0; i < roomInfo.getDealtBoardCards().size(); i++) {
					this.boardCards[i] = roomInfo.getDealtBoardCards().toByteArray()[i];
				}
			} else {
				for (int i = 0; i < this.boardCards.length; i++) {
					this.boardCards[i] = 0;
				}
			}
			break;
		}
		default: {
		}
		}

	}

	@Override
	public long getLastBeginTime() {
		return lastBeginTime;
	}

	@Override
	public int getType() {
		return this.type;
	}

	@Override
	public int getLevel() {
		return this.level;
	}
}
