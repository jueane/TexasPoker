package poker.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class BytebufTest {

	public static void main(String args[]) {
		ByteBuf buf = Unpooled.buffer(1024);

		byte[] bts = new byte[3];
		bts[0] = 33;
		bts[1] = 44;
		bts[2] = 55;

		System.out.println("read:" + buf.readerIndex() + "，write：" + buf.writerIndex() + "，capcity：" + buf.capacity() + "，readable：" + buf.readableBytes());

		
		buf.writeBytes(bts);

		System.out.println("read:" + buf.readerIndex() + "，write：" + buf.writerIndex() + "，capcity：" + buf.capacity() + "，readable：" + buf.readableBytes());
		byte[] dst = new byte[3];
		buf.readBytes(dst);

		System.out.println("read:" + buf.readerIndex() + "，write：" + buf.writerIndex() + "，capcity：" + buf.capacity() + "，readable：" + buf.readableBytes());

		
		buf.readerIndex(0);

		byte[] dstB = new byte[1];
		buf.readBytes(dstB);
		System.out.println("read:" + buf.readerIndex() + "，write：" + buf.writerIndex() + "，capcity：" + buf.capacity() + "，readable：" + buf.readableBytes());

		// System.out.println("dstB：" + dstB[0] + "," + dstB[1] + "," +
		// dstB[2]);

		buf.discardReadBytes();

		System.out.println("read:" + buf.readerIndex() + "，write：" + buf.writerIndex() + "，capcity：" + buf.capacity() + "，readable：" + buf.readableBytes());

	}

}
