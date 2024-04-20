package frankvicky.cc.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class SliceTest {
    private static final Logger logger = LoggerFactory.getLogger(AllocatorTest.class);
    @Test
    public  void testSlice() {
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(9, 100);
        logger.info("Action: Allocate ByteBuf(9, 100), {}", buffer);

        buffer.writeBytes(new byte[]{1, 2, 3, 4});
        logger.info("Action: Write 4 bytes (1,2,3,4), {}", buffer);


        ByteBuf slice = buffer.slice();
        logger.info("Action: Slice, {}", slice);

        byte[] dst = new byte[4];
        slice.readBytes(dst);
        System.out.println("dst = " + Arrays.toString(dst));
        logger.info("Action: slice after read, {}", slice);
        logger.info("Action: buffer after read, {}", buffer);

        buffer.readByte();
        logger.info("Action: buffer before slice to slice 1, {}", buffer);

        ByteBuf slice1 = buffer.slice();
        logger.info("Action: Slice slice1, {}", slice1);
        byte[] dst1 = new byte[3];
        slice1.readBytes(dst1);
        System.out.println("dst1 = " + Arrays.toString(dst1));

        buffer.retain();
        logger.info("4.0 refCnt(): " + buffer.refCnt());
        logger.info("4.0 slice refCnt(): " + slice.refCnt());
        logger.info("4.0 slice1 refCnt(): " + slice1.refCnt());
        buffer.release();
        logger.info("4.0 refCnt(): " + buffer.refCnt());
        logger.info("4.0 slice refCnt(): " + slice.refCnt());
        logger.info("4.0 slice1 refCnt(): " + slice1.refCnt());
    }

}
