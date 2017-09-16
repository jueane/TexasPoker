package main;

import java.util.Random;

import proto.ProtoFactory.ActionNoticeProto;
import proto.ProtoFactory.EnterRoomProto;
import proto.ProtoFactory.EnterRoomRespProto;
import proto.ProtoFactory.RaiseProto;
import proto.ProtoFactory.ValidationProto;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import entity.MemberInfo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ClientEventHandler extends ChannelInboundHandlerAdapter {
	private MemberInfo mem = null;
	private int roomId = 0;

	public ChannelHandlerContext channelCtx = null;
	private ByteBuf buf = null;

	public void setMem(MemberInfo mem) {
		this.mem = mem;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		validation();
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		buf = Unpooled.buffer(1024 * 10);
		ctx.fireChannelRegistered();
		channelCtx = ctx;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf in = (ByteBuf) msg;
		try {
			while (in.isReadable()) {
				buf.writeBytes(in);
			}
			while (true) {
				if (buf.readableBytes() < 8) {
					break;
				}
				// 获取head
				int iCode = 0;
				int iLength = 0;
				byte[] head = new byte[8];
				buf.readBytes(head);
				iCode |= head[0] & 0xff;
				iCode |= (head[1] << 8) & 0xffff;
				iCode |= (head[2] << 16) & 0xffffff;
				iCode |= (head[3] << 24) & 0xffffffff;
				iLength |= head[4] & 0xff;
				iLength |= (head[5] << 8) & 0xffff;
				iLength |= (head[6] << 16) & 0xffffff;
				iLength |= (head[7] << 24) & 0xffffffff;
				if (iLength < 0 || iLength > 1024 * 10) {
					if (iLength != 1414012975) {
						System.out.println("Msg length is negative or too long." + iLength);
					}
					ctx.close();
					return;
				}
				// 内容不完整，缓冲区回滚
				if (buf.readableBytes() < iLength) {
					buf.readerIndex(buf.readerIndex() - 8);
					// System.out.println("recv：" + iCode + "，" + iLength +
					// "，内容缺失，回滚。");
					break;
				}
				Pack pack = new Pack();
				pack.code = iCode;
				pack.length = iLength;
				// 获取 content
				if (iLength > 0) {
					pack.content = new byte[iLength];
					buf.readBytes(pack.content);
				}
				buf.discardReadBytes();
				// JLog.debugln("recv：" + iCode + "，" + iLength + "，" +
				// iLength);
				packProcessor(pack);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		((ByteBuf) msg).release();
	}

	private void packProcessor(Pack pack) {
		switch (pack.code) {
		case 3366: {
			System.out.println("Player " + this.mem.getId() + " Rec test.");
			break;
		}
		case 711013: {
			// 验证成功
			EnterRoomProto.Builder enterRoomProto = EnterRoomProto.newBuilder();
			enterRoomProto.setRoomType(Launch.roomType);
			enterRoomProto.setRoomLevel(Launch.roomLevel);
			send(721020, enterRoomProto.build().toByteArray());// 请求进入房间
			// System.out.println("Done");
			break;
		}
		case 711015: {
			System.out.println("Failed");
			break;
		}
		case 711025: {
			EnterRoomRespProto enterResp = null;
			try {
				enterResp = EnterRoomRespProto.parseFrom(ByteString.copyFrom(pack.content));
				this.roomId = enterResp.getRoomId();
				if (this.roomId > 0) {
					System.out.println("Player " + mem.getId() + " enter type " + enterResp.getRoomType() + " level " + Launch.roomLevel + " room " + enterResp.getRoomId() + " seat "
							+ enterResp.getSeat());
					enableStatusCheck();
				}
			} catch (InvalidProtocolBufferException e) {
				System.err.println("EnterResult data format error.");
			}
			break;
		}
		case 712020: {
			// 下注
			action(pack.content);
			break;
		}
		default:
			break;
		}

	}

	// 下注
	private void action(byte[] buff) {
		ActionNoticeProto actionNoticeProto = null;
		try {
			actionNoticeProto = ActionNoticeProto.parseFrom(buff);
		} catch (InvalidProtocolBufferException e1) {
			e1.printStackTrace();
		}
		if (actionNoticeProto.getPlayerId() == mem.getId()) {
			System.out.println("Player " + mem.getId() + " acting...");
			// System.out.print("Player " + mem.getId() + " Bankroll：" +
			// actionNoticeProto.getRemainBankroll());
			// System.out.print("，mincall：" + actionNoticeProto.getMinCall());
			// System.out.print("；");
			// // 下注
			// System.out.print("1.让牌，2.跟注，3.加注，4.all in，5.弃牌...");
			// System.out.print("act：");
			try {
				Thread.sleep(Launch.actionTimeout);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// int act = getInt(1, 5);
			int act = 0;
			int randomInt = new Random().nextInt(10);
			if (actionNoticeProto.getMinRaise() <= actionNoticeProto.getRemainBankroll() && randomInt >= 8) {
				act = 3;
			} else if (actionNoticeProto.getCheckable() && randomInt > 3) {
				act = 1;
			} else if (actionNoticeProto.getRemainBankroll() > 0) {
				if (actionNoticeProto.getMinCall() < actionNoticeProto.getRemainBankroll()) {
					act = 2;
				} else {
					// all in 或弃
					if (randomInt >= 5) {
						act = 4;
					} else {
						act = 5;
					}
				}
			}
			if (act <= 0 || act > 5) {
				act = 5;
			}
			switch (act) {
			case 1:
				send(722031, null);
				break;
			case 2:
				send(722032, null);
				break;
			case 3:
				int min = actionNoticeProto.getMinRaise();
				int max = actionNoticeProto.getRemainBankroll();
				Random rdm = new Random();
				int ante = rdm.nextInt(max);
				if (ante < min) {
					ante = min;
				}
				RaiseProto.Builder raiseBuilder = RaiseProto.newBuilder();
				raiseBuilder.setAnte(ante);
				send(722033, raiseBuilder.build().toByteArray());
				break;
			case 4:
				send(722034, null);
				break;
			case 5:
				send(722035, null);
				break;
			default:
				System.out.println("Send nothing.");
				break;
			}
		} else {
			// System.out.println("Id为" + actionNoticeProto.getPlayerId() +
			// "的玩家正在下注...");
		}
	}

	// 验证
	private void validation() {
		// System.out.print("Validating...");
		ValidationProto.Builder validationBuilder = ValidationProto.newBuilder();
		validationBuilder.setToken(mem.getToken());
		send(721010, validationBuilder.build().toByteArray());
		System.out.println("Player " + mem.getId() + " validating.");
	}

	// 启动心跳
	private void enableStatusCheck() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(30000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					send(721000, null);
				}
			}
		}).start();
	}

	public void send(int code, byte[] bytes) {
		if (channelCtx == null) {
			return;
		}
		ByteBuf buf = Unpooled.buffer((bytes == null ? 0 : bytes.length) + 8);
		int length = bytes == null ? 0 : bytes.length;
		byte[] header = new byte[8];
		header[0] = (byte) (code & 0xff);
		header[1] = (byte) ((code >> 8) & 0xff);
		header[2] = (byte) ((code >> 16) & 0xff);
		header[3] = (byte) ((code >> 24) & 0xff);
		header[4] = (byte) (length & 0xff);
		header[5] = (byte) ((length >> 8) & 0xff);
		header[6] = (byte) ((length >> 16) & 0xff);
		header[7] = (byte) ((length >> 24) & 0xff);
		// if (player != null) {
		// System.out.print("[" + player.info.getUsername() + "]");
		// }
		// JLog.debugln("send：" + code + "，" + length);
		buf.writeBytes(header);
		if (bytes != null) {
			buf.writeBytes(bytes);
		}
		channelCtx.writeAndFlush(buf);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.out.println("Caught a exception.");
		super.exceptionCaught(ctx, cause);
	}

	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
		System.out.println("writability改变。isWritable:" + ctx.channel().isWritable() + ",isActive:" + ctx.channel().isActive());
		super.channelWritabilityChanged(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("Id " + mem.getId() + " active改变。isWritable:" + ctx.channel().isWritable() + ",isActive:" + ctx.channel().isActive());
		super.channelInactive(ctx);
	}

}
