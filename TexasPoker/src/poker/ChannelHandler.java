package poker;

import poker.main.player.ChannelEventProxy;
import poker.util.JLog;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ChannelHandler extends ChannelInboundHandlerAdapter {

	public ChannelHandlerContext channelCtx = null;
	protected ByteBuf buf = null;
	private ChannelEventProxy eventProxy;
	protected static JLog log = new JLog("EventHandler");

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		buf = Unpooled.buffer(1024 * 10);
		ctx.fireChannelRegistered();
		channelCtx = ctx;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		ByteBuf in = (ByteBuf) msg;
		try {
			buf.writeBytes(in);
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
						log.debugln("Packet length error.Length:" + iLength + ",hash:" + this.hashCode());
					}
					ctx.close();
					return;
				}
				// 内容不完整，缓冲区回滚
				if (buf.readableBytes() < iLength) {
					buf.readerIndex(buf.readerIndex() - 8);
					log.debugln("recv:" + iCode + "," + iLength + ",roll back.");
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
		eventProxy = new ChannelEventProxy(this);
		eventProxy.handle(pack.code, pack.content);
	}

	public void send(int code, byte[] bytes) {
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
		log.debugln("Channel exception caught.");
		cause.printStackTrace();
		// 通道异常
		eventProxy.connectionException();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		log.debugln("Channel inactive.");
		super.channelInactive(ctx);
	}

}
