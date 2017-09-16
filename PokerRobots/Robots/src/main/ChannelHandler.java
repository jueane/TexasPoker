package main;

import java.util.Date;

import main.robot.Robot;
import proto.ProtoFactory.CardsProto;
import proto.ProtoFactory.EnterRoomProto;
import proto.ProtoFactory.EnterRoomRespProto;
import proto.ProtoFactory.ValidationProto;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import data.MemberData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ChannelHandler extends ChannelInboundHandlerAdapter {
	public Robot robot;
	private int roomId = 0;
	boolean stateCheckOpen = true;

	public ChannelHandlerContext channelCtx = null;
	private ByteBuf buf = null;

	private void packProcessor(Pack pack) {
		switch (pack.code) {
		case 3366: {
			System.out.println("Robot " + robot.getUsernfo().getId() + " Rec test.");
			break;
		}
		case 711013: {
			// 验证成功
			EnterRoom();
			break;
		}
		case 711015: {
			System.out.println("Failed");
			break;
		}
		case 711025: {
			EnterRoomRespProto enterResp = null;
			try {
				enterResp = EnterRoomRespProto.parseFrom(ByteString.copyFrom(pack.data));
				this.roomId = enterResp.getRoomId();
				if (this.roomId > 0) {
					enableStatusCheck();
				}
			} catch (InvalidProtocolBufferException e) {
				System.err.println("EnterResult data format error.");
			}
			break;
		}
		case 711055:// 筹码补充失败// 此外不加break;.使其进入711067
		case 711067: {
			// 坐下失败，金币数量不足以兑换minTake.
			MemberData.setMemberGold(robot.getUsernfo().getId(), 200000);
			robot.restart();
			break;
		}
		case 712020: {
			// 下注
			robot.action(pack.data);
			break;
		}
		case 712010: {
			// 发底牌
			CardsProto cardsProto = null;
			try {
				cardsProto = CardsProto.parseFrom(pack.data);
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
			byte[] holeCards = cardsProto.getCards().toByteArray();
			robot.setHoleCards(holeCards);
			break;
		}
		case 712070: {
			CardsProto cardsProto = null;
			try {
				cardsProto = CardsProto.parseFrom(pack.data);
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
			this.robot.broadcastCards(cardsProto.getCards().toByteArray());
			break;
		}
		case 712080: {
			robot.gameEnd();
			break;
		}
		case 712100: {
			// 房间停止
			break;
		}
		default:
			break;
		}

	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		stateCheckOpen = true;
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
		try {
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
						pack.data = new byte[iLength];
						buf.readBytes(pack.data);
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
		} catch (Exception e) {
			System.out.println("Pack analysis error.");
		}
	}

	private void EnterRoom() {
		System.out.println("[Enter]Player " + robot.getUsernfo().getId() + " enter type " + robot.getType() + " level " + robot.getLevel() + ".");
		EnterRoomProto.Builder enterRoomProto = EnterRoomProto.newBuilder();
		enterRoomProto.setRoomId(-100);
		enterRoomProto.setRoomType(robot.getType());
		enterRoomProto.setRoomLevel(robot.getLevel());
		send(721020, enterRoomProto.build().toByteArray());// 请求进入房间
	}

	// 验证
	private void validation() {
		ValidationProto.Builder validationBuilder = ValidationProto.newBuilder();
		validationBuilder.setToken(robot.getUsernfo().getToken());
		send(721010, validationBuilder.build().toByteArray());
	}

	// 启动心跳
	private void enableStatusCheck() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (stateCheckOpen) {
					robot.setTime(new Date().getTime());
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
		System.out.println("Caught a exception in eventhandler.");
		super.exceptionCaught(ctx, cause);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		System.out.println("Channel inactive.");
		stateCheckOpen = false;

	}

}
