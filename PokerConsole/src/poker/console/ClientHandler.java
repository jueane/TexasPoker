package poker.console;

import java.util.Date;
import java.util.Iterator;

import com.google.protobuf.InvalidProtocolBufferException;

import poker.console.ProtoFactoryForConsole.PlayerProto;
import poker.console.ProtoFactoryForConsole.RoomInfoProto;
import poker.console.ProtoFactoryForConsole.RoomListProto;
import poker.console.ProtoFactoryForConsole.RoomListProto.RoomProto;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ClientHandler extends ChannelInboundHandlerAdapter {
	public static int uniqueNumber = 1;

	public long createTime;

	public static ChannelHandlerContext channelCtx = null;
	private ByteBuf buf = null;

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		buf = Unpooled.buffer(1024 * 10);
		ctx.fireChannelRegistered();
		channelCtx = ctx;
		createTime = new Date().getTime();
		if (PokerConsole.client != null) {
			PokerConsole.client.textArea.setText(PokerConsole.client.textArea.getText() + "Connected.");
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
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
						System.out.println("Packet length error.Length:" + iLength + ",hash:" + this.hashCode());
					}
					ctx.close();
					return;
				}
				// 内容不完整，缓冲区回滚
				if (buf.readableBytes() < iLength) {
					buf.readerIndex(buf.readerIndex() - 8);
					System.out.println("recv：" + iCode + "，" + iLength + "，内容缺失，回滚。");
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
				// System.out.println("recv：" + iCode + "，" + iLength + "，" +
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
		case 721010: {
			long beginTime = new Date().getTime();
			long endTime = new Date().getTime();
			System.out.println("Validation use time：" + (endTime - beginTime));
			break;
		}
		case 721020: {
			long beginTime = new Date().getTime();
			long endTime = new Date().getTime();
			System.out.println("Enter room use time：" + (endTime - beginTime));
			break;
		}
		case 101: {
			RoomInfoProto roomInfoProto = null;
			try {
				roomInfoProto = RoomInfoProto.parseFrom(pack.content);
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
			PokerConsole.client.textArea.append("\nRoom " + roomInfoProto.getRoomId() + "\n");
			PokerConsole.client.textArea.append("smallBlind:" + roomInfoProto.getSmallBlind() + "\n");
			PokerConsole.client.textArea.append("minTake:" + roomInfoProto.getMinTake() + "\n");
			PokerConsole.client.textArea.append("maxTake:" + roomInfoProto.getMaxTake() + "\n");
			PokerConsole.client.textArea.append("bankerSeat:" + roomInfoProto.getBankerSeat() + "\n");
			PokerConsole.client.textArea.append("smallBlindSeat:" + roomInfoProto.getSmallBlindSeat() + "\n");
			PokerConsole.client.textArea.append("smallBlindBetted:" + roomInfoProto.getSmallBlindBetted() + "\n");
			PokerConsole.client.textArea.append("bigBlindSeat:" + roomInfoProto.getBigBlindSeat() + "\n");
			PokerConsole.client.textArea.append("bigBlindBetted:" + roomInfoProto.getBigBlindBetted() + "\n");
			PokerConsole.client.textArea.append("phase:" + roomInfoProto.getGamePhase() + "\n");
			PokerConsole.client.textArea.append("turn:" + roomInfoProto.getTurn() + "\n");
			PokerConsole.client.textArea.append("bettingSeat:" + roomInfoProto.getBettingSeat() + "\n");
			PokerConsole.client.textArea.append("dealtBoardCards(" + roomInfoProto.getDealtBoardCards().size() + "):");
			showCards(roomInfoProto.getDealtBoardCards().toByteArray());
			PokerConsole.client.textArea.append("player count:" + roomInfoProto.getPlayerListCount() + "\n");
			Iterator<PlayerProto> playerProtoItr = roomInfoProto.getPlayerListList().iterator();
			while (playerProtoItr.hasNext()) {
				PlayerProto playerProto = playerProtoItr.next();
				PokerConsole.client.textArea.append("   player " + playerProto.getNickname() + " id " + playerProto.getPlayerId() + " in " + playerProto.getSeat() + " has bankroll:"
						+ playerProto.getBankRoll() + " gold:" + playerProto.getGold() + " antes:" + playerProto.getAnteListList().toString() + "\n");

			}
			break;
		}
		case 102: {
			RoomListProto roomListProto = null;
			try {
				roomListProto = RoomListProto.parseFrom(pack.content);
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
			for (int i = 0; i < roomListProto.getRoomListCount(); i++) {
				RoomProto roomProto = roomListProto.getRoomList(i);
				PokerConsole.client.textArea.append("\nRoom " + roomProto.getRoomId());
				PokerConsole.client.textArea.append(",type " + roomProto.getRoomType());
				PokerConsole.client.textArea.append(",level " + roomProto.getRoomLevel());
				PokerConsole.client.textArea.append(",phase " + roomProto.getRoomPhase());
				PokerConsole.client.textArea.append(",player " + roomProto.getPlayingCount() + "/" + roomProto.getSittingCount() + "/" + roomProto.getSeatsCount());
				PokerConsole.client.textArea.append(",betable " + roomProto.getBetableCount());
				PokerConsole.client.textArea.append(",noFold " + roomProto.getNoFoldCount());
			}
			break;
		}
		default:
			break;
		}

	}

	private static void showCard(byte card) {
		int color = (card >> 4) & 0xf;
		if (color == 1) {
			PokerConsole.client.textArea.append("黑");
		} else if (color == 2) {
			PokerConsole.client.textArea.append("红");
		} else if (color == 3) {
			PokerConsole.client.textArea.append("梅");
		} else if (color == 4) {
			PokerConsole.client.textArea.append("方");
		}
		int a = card & 0xf;
		if (a == 14) {
			PokerConsole.client.textArea.append("A");
		} else {
			PokerConsole.client.textArea.append("" + a);
		}
	}

	public static void showCards(byte[] cards) {
		for (int i = 0; i < cards.length; i++) {
			showCard(cards[i]);
			if (i < cards.length - 1) {
				PokerConsole.client.textArea.append("，");
			}
		}
		PokerConsole.client.textArea.append("\r\n");
	}

	public static void send(int code, byte[] bytes) {
		if (channelCtx == null) {
			PokerConsole.client.textArea.setText(PokerConsole.client.textArea.getText() + "No connection." + "\n");
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
		buf.writeBytes(header);
		if (bytes != null) {
			buf.writeBytes(bytes);
		}
		channelCtx.writeAndFlush(buf);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		System.out.println("hash：" + this.hashCode() + "，断开");
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("active改变。isWritable:" + ctx.channel().isWritable() + ",isActive:" + ctx.channel().isActive());
		super.channelInactive(ctx);
	}

}
