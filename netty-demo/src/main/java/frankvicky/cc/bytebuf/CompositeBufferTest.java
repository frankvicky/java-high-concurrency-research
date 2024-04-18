package frankvicky.cc.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class CompositeBufferTest {

    private static final Logger logger = LoggerFactory.getLogger(CompositeBufferTest.class);

    @Test
    public void intCompositeBufComposite() {
        CompositeByteBuf compositeByteBuf = Unpooled.compositeBuffer(3);
        compositeByteBuf.addComponent(Unpooled.wrappedBuffer(new byte[]{1, 2, 3}));
        compositeByteBuf.addComponent(Unpooled.wrappedBuffer(new byte[]{4}));
        compositeByteBuf.addComponent(Unpooled.wrappedBuffer(new byte[]{5, 6}));

        logger.info("Action: addComponent, {}", compositeByteBuf);
        showMsg(compositeByteBuf);
        iterateMsg(compositeByteBuf);

        // 合併成一個單獨的 buffer
        ByteBuffer nioBuffer = compositeByteBuf.nioBuffer(0, 6);


        byte[] bytes = nioBuffer.array();
        System.out.print("bytes = ");
        for (byte b : bytes) {
            System.out.print(b);
        }
        compositeByteBuf.release();
    }

    @Test
    public void byteBufComposite() {
        // header
        ByteBuf headerBuf = Unpooled.wrappedBuffer(("疯狂创客圈:").getBytes());
        // body
        ByteBuf bodyBuf = Unpooled.wrappedBuffer(("高性能 Netty").getBytes());

        // deep Copy
        ByteBuf dstBuf = ByteBufAllocator.DEFAULT.buffer();
        dstBuf.writeBytes(headerBuf.slice());
        dstBuf.writeBytes(bodyBuf.slice());
        logger.info("Action：dstBuf, {}", dstBuf);
        showMsg(dstBuf);

        // zero copy
        CompositeByteBuf compositeByteBuf = ByteBufAllocator.DEFAULT.compositeBuffer();
        compositeByteBuf.addComponents(headerBuf.slice(), bodyBuf.slice());
        logger.info("Action：addComponent 1, {}", compositeByteBuf);
        showMsg(compositeByteBuf);

        iterateMsg(compositeByteBuf);

        headerBuf.retain();
        compositeByteBuf.release();

        compositeByteBuf = Unpooled.compositeBuffer(2);

        // body2
        bodyBuf = Unpooled.wrappedBuffer(("高性能学习社群, 卷王 社群").getBytes());
        compositeByteBuf.addComponents(headerBuf.slice(), bodyBuf.slice());


        logger.info("Action：addComponent 2, {}", compositeByteBuf);
        showMsg(compositeByteBuf);

        iterateMsg(compositeByteBuf);

        compositeByteBuf.release();
    }


    private void showMsg(ByteBuf b) {
        System.out.println("showMsg ..........");
        // 處理整個消息
        int length = b.readableBytes();
        byte[] array = new byte[length];
        // 把 CompositeByteBuf 中的資料複製到 byte array
        b.getBytes(b.readerIndex(), array);
        // 處理一下 byte array 裡的資料
        System.out.println("content： " + new String(array));
    }

    private void iterateMsg(CompositeByteBuf cbuf) {
        System.out.println("iterateMsg .......... ");

        Iterator<ByteBuf> it = cbuf.iterator();

        while (it.hasNext()) {
            ByteBuf b = it.next();
            int length = b.readableBytes();
            byte[] array = new byte[length];
            // 把 CompositeByteBuf 中的資料複製到 byte array
            b.getBytes(b.readerIndex(), array);
            System.out.print(new String(array, StandardCharsets.UTF_8));
        }

        System.out.println();

        //处理整个消息
        for (ByteBuf b : cbuf) { // for in
            int length = b.readableBytes();
            byte[] array = new byte[length];
            // 把 CompositeByteBuf 中的資料複製到 byte array
            b.getBytes(b.readerIndex(), array);
            System.out.print(new String(array, StandardCharsets.UTF_8));
        }
        System.out.println();
    }


    @Test
    public void byteBufWrapper() {
        // header
        ByteBuf headerBuf = Unpooled.wrappedBuffer(("疯狂创客圈:").getBytes());
        // body
        ByteBuf bodyBuf = Unpooled.wrappedBuffer(("高性能 Netty").getBytes());


        logger.info("动作：headerBuf, {}", headerBuf);
        showMsg(headerBuf);

        // zero copy
        ByteBuf wrappedBuffer = Unpooled.wrappedBuffer(headerBuf.slice(), bodyBuf.slice());
        logger.info("动作：addComponent 1, {}", wrappedBuffer);


        showMsg(wrappedBuffer);
        iterateMsg((CompositeByteBuf) wrappedBuffer);


        headerBuf.retain();
        wrappedBuffer.release();


        // body2
        bodyBuf = Unpooled.wrappedBuffer(("高性能学习社群, 卷王 社群").getBytes());
        wrappedBuffer = Unpooled.wrappedBuffer(headerBuf.slice(), bodyBuf.slice());


        logger.info("Action：addComponent 2, {}", wrappedBuffer);

        showMsg(wrappedBuffer);

        iterateMsg((CompositeByteBuf) wrappedBuffer);
        wrappedBuffer.release();
    }
}
