package frankvicky.cc.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReferenceTest {
    private static final Logger logger = LoggerFactory.getLogger(ReferenceTest.class);

    @Test
    public void testRef() {

        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        logger.info("after create: {}", buffer.refCnt());

        buffer.retain();
        logger.info("after retain: {}", buffer.refCnt());

        buffer.release();
        logger.info("after release: {}", buffer.refCnt());

        buffer.release();
        logger.info("after release: {}", buffer.refCnt());

        // Error: refCnt 為 0, 不能再 retain
        buffer.retain();
        logger.info("after retain:{}", buffer.refCnt());
    }


}
