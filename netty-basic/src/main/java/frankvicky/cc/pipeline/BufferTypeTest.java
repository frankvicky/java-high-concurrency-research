package frankvicky.cc.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class BufferTypeTest {
    private static final Logger logger = LoggerFactory.getLogger(AllocatorTest.class);
    private final static Charset UTF_8 = StandardCharsets.UTF_8;

    // HeapBuffer
    @Test
    public void testHeapBuffer() {
        // 取得 Heap Memory
        // 取得 Heap Memory --netty4預設使用 Direct Buffer，而非 Heap Buffer
        // ByteBuf heapBuf = ByteBufAllocator.DEFAULT.buffer();
        ByteBuf heapBuf = ByteBufAllocator.DEFAULT.heapBuffer();
        heapBuf.writeBytes("疯狂创客圈:高性能学习社群".getBytes(UTF_8));

        if (heapBuf.hasArray()) {
            //取得内部 array
            byte[] array = heapBuf.array();
            int offset = heapBuf.arrayOffset() + heapBuf.readerIndex();
            int length = heapBuf.readableBytes();
            logger.info(new String(array, offset, length, UTF_8));
        }

        heapBuf.release();
    }

    // DirectBuffer
    @Test
    public void testDirectBuffer() {
        ByteBuf directBuf = ByteBufAllocator.DEFAULT.directBuffer();
        directBuf.writeBytes("疯狂创客圈:高性能学习社群".getBytes(UTF_8));
        if (!directBuf.hasArray()) {
            int length = directBuf.readableBytes();
            byte[] array = new byte[length];
            // 從 Direct Buffer 讀取資料到 Heap Memory
            directBuf.getBytes(directBuf.readerIndex(), array);
            logger.info(new String(array, UTF_8));
        }
        directBuf.release();
    }
}
