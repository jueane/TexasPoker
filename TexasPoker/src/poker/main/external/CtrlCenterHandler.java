package poker.main.external;

import com.google.protobuf.InvalidProtocolBufferException;

import poker.main.ChannelHandler;
import poker.main.Pack;
import poker.proto.ProtoFactoryForCtrlCenter.CCPlayerStausProto;
import poker.proto.ProtoFactoryForCtrlCenter.CCValidationProto;
import poker.util.JLog;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class CtrlCenterHandler extends ChannelInboundHandlerAdapter {
	private static final String TOKEN_FOR_CC = "pingames_pokerserver001";

	public ChannelHandlerContext channelCtx = null;
	private ByteBuf buf = null;

	static JLog log = new JLog("CtrlCenterHandler");

	public class Message {
		public int code;
		public byte[] buff;
		public boolean processed;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		CCValidationProto.Builder ccvBuilder = CCValidationProto.newBuilder();
		ccvBuilder.setToken(TOKEN_FOR_CC);
		send(120710, ccvBuilder.build().toByteArray());
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
						log.debugln("Msg length is negative or too long." + iLength);
					}
					ctx.close();
					return;
				}
				// 内容不完整，缓冲区回滚
				if (buf.readableBytes() < iLength) {
					buf.readerIndex(buf.readerIndex() - 8);
					log.debugln("recv：" + iCode + "，" + iLength + "，内容缺失，回滚。");
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
				// log.debugln("recv：" + iCode + "，" + iLength + "，" +
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
		case 110713: {
			log.infoln("Connect control center succeed！");
			break;
		}
		case 110715: {
			log.infoln("Connect control center failed！");
			break;
		}
		case 111701:
			// 中控应答用户状态
			if (pack.content == null) {
				log.errorln("Content is null in CCPlayerStausProto.");
				return;
			}
			CCPlayerStausProto ccpProto = null;
			try {
				ccpProto = CCPlayerStausProto.parseFrom(pack.content);
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
			if (ccpProto != null) {
				log.debugln("用户" + ccpProto.getPlayerId() + "状态为" + ccpProto.getStatus());
			}
			break;
		case 111702: {
			// 中控请求玩家状态
			playerStatusResp(pack.content);
			break;
		}
		default:
			break;
		}

	}

	// 应答中控的用户状态请求
	private void playerStatusResp(byte[] content) {
		if (content == null) {
			log.errorln("Content is null in playerStatusResp.");
			return;
		}
		CCPlayerStausProto playerStausProtoReq = null;
		try {
			playerStausProtoReq = CCPlayerStausProto.parseFrom(content);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
			return;
		}
		int playerStatus = 0;
		for (int i = 0; i < ChannelHandler.roomList.size(); i++) {
			playerStatus = ChannelHandler.roomList.get(i).playerExist(playerStausProtoReq.getPlayerId());
			if (playerStatus > 0) {
				break;
			}
		}
		if (playerStatus == 0) {
			for (int i = 0; i < ChannelHandler.roomKnockList.size(); i++) {
				playerStatus = ChannelHandler.roomKnockList.get(i).playerExist(playerStausProtoReq.getPlayerId());
				if (playerStatus > 0) {
					break;
				}
			}
		}
		if (playerStatus == 0) {
			for (int i = 0; i < ChannelHandler.roomImperialList.size(); i++) {
				playerStatus = ChannelHandler.roomImperialList.get(i).playerExist(playerStausProtoReq.getPlayerId());
				if (playerStatus > 0) {
					break;
				}
			}
		}
		if (playerStatus == 0) {
			for (int i = 0; i < ChannelHandler.roomOmahaList.size(); i++) {
				playerStatus = ChannelHandler.roomOmahaList.get(i).playerExist(playerStausProtoReq.getPlayerId());
				if (playerStatus > 0) {
					break;
				}
			}
		}
		CCPlayerStausProto.Builder playerStausProtoResp = CCPlayerStausProto.newBuilder();
		playerStausProtoResp.setPlayerId(playerStausProtoReq.getPlayerId());
		playerStausProtoResp.setStatus(playerStatus);
		send(111702, playerStausProtoResp.build().toByteArray());
	}

	protected void send(int code, byte[] bytes) {
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
		// log.debugln("send：" + code + "，" + length);
		buf.writeBytes(header);
		if (bytes != null) {
			buf.writeBytes(bytes);
		}
		channelCtx.writeAndFlush(buf);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// TODO Auto-generated method stub
		super.exceptionCaught(ctx, cause);
	}

	// 业务方法

	public void noticePlayerJoin(int playerId) {
		try {
			CCPlayerStausProto.Builder playerStausProto = CCPlayerStausProto.newBuilder();
			playerStausProto.setPlayerId(playerId);
			playerStausProto.setStatus(1);
			CtrlCenterSync.ctrlCenterHandler.send(121703, playerStausProto.build().toByteArray());
			log.debugln("Notice ctrlCenter player " + playerId + " joined.");
		} catch (Exception e) {
			log.debugln("Notice ctrlCenter failed." + e.getMessage());
		}
	}

	public void noticePlayerLeave(int playerId) {
		try {
			CCPlayerStausProto.Builder playerStausProto = CCPlayerStausProto.newBuilder();
			playerStausProto.setPlayerId(playerId);
			playerStausProto.setStatus(1);
			send(121704, playerStausProto.build().toByteArray());
			log.debugln("Notice ctrlCenter player " + playerId + " has left.");
		} catch (Exception e) {
			log.debugln("Notice ctrlCenter failed." + e.getMessage());
		}

	}

}
