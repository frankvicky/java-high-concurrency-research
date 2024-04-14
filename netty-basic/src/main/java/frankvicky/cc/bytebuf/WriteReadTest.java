package frankvicky.cc.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WriteReadTest {
    private static final Logger logger = LoggerFactory.getLogger(WriteReadTest.class);

    @Test
    public void testWriteRead() {
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(9, 100);
        logger.info("Allocate ByteBuf(9, 100), {}", buffer);

        buffer.writeBytes(new byte[]{1, 2, 3, 4});
        logger.info("Write 4 bytes (1, 2, 3, 4), {}", buffer);
        logger.info("Starting================:get:================");
        getByteBuf(buffer);
        logger.info("Get ByteBuf, {}", buffer);
        logger.info("Ending================:read:================");
        readByteBuf(buffer);
        logger.info("Read ByteBuf, {}", buffer);
        logger.info("Ending==================:end:================");
    }

    private void getByteBuf(ByteBuf buffer) {
        for (int i = 0; i < buffer.readableBytes(); i++) {
            // 取 byte，不會改變 readerIndex
            logger.info("Get a byte: {}", buffer.getByte(i));// 0,1,2,3
        }
    }

    private void readByteBuf(ByteBuf buffer) {
        while (buffer.isReadable()) {
            // 讀 byte，會改變 readerIndex
            logger.info("Read a byte: {}", buffer.readByte());
        }
    }

    @Test
    public void testResize() {
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(10,1024);
        logger.info("Allocate ByteBuf(4), {}", buffer);

        logger.info("start==========:write 4 bytes:==========");
        buffer.writeBytes(new byte[]{1, 2, 3, 4});
        logger.info("Write 4 Bytes, {}", buffer);


        logger.info("start==========:write 10 bytes:==========");
        buffer.writeBytes(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11});
        logger.info("Write 10 Bytes, {}", buffer);

        logger.info("start==========:write 64 bytes:==========");
        for (int i = 0; i < 64; i++) {
            buffer.writeByte(1);
        }
        logger.info("Write 64 Bytes, {}", buffer);


        logger.info("start==========:write 128 bytes:==========");
        for (int i = 0; i < 128; i++) {
            buffer.writeByte(1);
        }
        logger.info("Write 128 Bytes, {}", buffer);
    }
}
