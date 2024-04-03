package frankvicky.cc.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.IntBuffer;

public class UseBuffer {
    private static final Logger logger = LoggerFactory.getLogger(UseBuffer.class);

    private static IntBuffer intBuffer = null;

    public static void allocateTest() {
        intBuffer = IntBuffer.allocate(20);
        logger.debug("----------after allocate----------");
        logBufferField();
    }

    public static void putTest() {
        for (int i = 0; i < 5; i++) {
            intBuffer.put(i);
        }
        logger.debug("----------after put----------");
        logBufferField();
    }

    public static void flipTest() {
        // 寫模式翻轉為讀模式
        // mark 會被重置為 -1
        intBuffer.flip();
        logger.debug("----------after flip----------");
        logBufferField();
    }

    public static void getTest() {
        for (int i = 0; i < 2; i++) {
            int value = intBuffer.get();
            logger.debug("value = " + value);
        }

        logger.debug("----------after get 2 int----------");
        logBufferField();

        for (int i = 0; i < 3; i++) {
            int value = intBuffer.get();
            logger.debug("value = " + value);
        }

        logger.debug("----------after get 3 int----------");
        logBufferField();
    }

    public static void rewindTest() {
        // 倒帶讀指針
        // mark 會被重置為 -1
        intBuffer.rewind();
        logger.debug("----------after rewind----------");
        logBufferField();
    }

    public static void reRead() {
        for (int i = 0; i < 5; i++) {
            if (i == 2) {
                logger.debug("mark at index " + i);
                intBuffer.mark();
            }

            int value = intBuffer.get();
            logger.debug("value = " + value);
        }

        logger.debug("----------after reRead----------");
        logBufferField();
    }

    public static void afterReset() {
        // reset 是重置讀指針到 mark 的位置，因此 mark 和 reset 會混合使用
        logger.debug("----------after reset----------");
        intBuffer.reset();
        logBufferField();

        for (int i = 2; i < 5; i++) {
            int value = intBuffer.get();
            logger.debug("value = " + value);
        }
    }

    public static void clearDemo() {
        // clear 方法會清空 buffer
        // position 會歸零, limit 會被設置為 capacity
        // 如果是在讀模式會被會被切換為寫模式
        logger.debug("----------after clear----------");
        intBuffer.clear();
        logBufferField();
    }

    private static void logBufferField() {
        logger.debug("position = " + intBuffer.position());
        logger.debug("limit = " + intBuffer.limit());
        logger.debug("capacity = " + intBuffer.capacity());
    }

    public static void main(String[] args) {
        allocateTest();
        putTest();
        flipTest();
        getTest();
        rewindTest();
        reRead();
        afterReset();
        clearDemo();
    }
}
