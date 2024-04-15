package frankvicky.cc.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// PoolByteBufAllocator（池化的 ByteBuf 分配器）將ByteBuf實例放入池中，提高了性能，將內存碎片減少到最小
// 池化分配器採用了類 jemalloc 的高效內存分配的策略，該策略被好幾種現代操作系統所采用。

// UnpooledByteBufAllocator 是普通的未池化ByteBuf分配器，它沒有把ByteBuf放入池中，每次被調用時，返回一個新的 ByteBuf 實例
// 使用完之後，通過 Java 的垃圾回收機制回收或者直接釋放（對於直接內存而言）。
public class AllocatorTest {
    private static final Logger logger = LoggerFactory.getLogger(AllocatorTest.class);

    //Xmx1000m -Xms1000m
    @Test
    public void showUnpooledByteBufAllocator() {
        UnpooledByteBufAllocator allocator = UnpooledByteBufAllocator.DEFAULT;
        logger.info("Memory allocate: {}", Runtime.getRuntime().maxMemory());

        for (int i = 0; i < 1000; i++) {
            ByteBuf buffer = allocator.directBuffer(20 * 1024 * 1024);
//            buffer.release();
            logger.info("{}", buffer);
            logger.info("Allocate {} MB", 20 * (i + 1));
        }

//        for (int i = 0; i < 1000; i++) {
//            ByteBuf buffer = allocator.heapBuffer(20 * 1024 * 1024);
////            buffer.release();
//            Logger.tcfo(buffer);
//            System.out.println("分配了 " + 20 * (i + 1) + " MB");
//        }

//        for (int i = 0; i < 1000; i++) {
//            ByteBuf buffer = allocator.buffer(20 * 1024 * 1024);
//            buffer.release();
//            Logger.tcfo(buffer);
//            System.out.println("分配了 " + 20 * (i + 1) + " MB");
//        }

    }

    //Xmx1000m -Xms1000m
    @Test
    public void showPooledByteBufAllocator() {
        PooledByteBufAllocator allocator = PooledByteBufAllocator.DEFAULT;

        for (int i = 0; i < 1000; i++) {
            ByteBuf buffer = allocator.directBuffer(20 * 1024 * 1024);
//            buffer.release();
            logger.info("{}", buffer);
            logger.info("Allocate {} MB", 20 * (i + 1));
        }
    }

    @Test
    public void showAlloc() {


        ByteBuf buffer = null;
        // 方法一：Default Allocator，分配初始容量為 9，最大容量為 100 的 buffer
        buffer = ByteBufAllocator.DEFAULT.buffer(9, 100);

        logger.info("{}", buffer);
        // 方法二：Default Allocator，分配初始為 256，最大容量為 Integer.MAX_VALUE 的 buffer
        // 空建構子會預設會設置最大容量為 Integer.MAX_VALUE
        buffer = ByteBufAllocator.DEFAULT.buffer();
        logger.info("{}", buffer);

        // 方法三：Unpooled ByteBuf Allocator，分配 Java-based 的 Heap Memory Buffer
        buffer = UnpooledByteBufAllocator.DEFAULT.heapBuffer();
        logger.info("{}", buffer);

        //方法四：Pooled ByteBuf Allocator，分配 OS-based 的 Direct Memory Buffer
        buffer = PooledByteBufAllocator.DEFAULT.directBuffer();
        logger.info("{}", buffer);
    }

    @Test
    public void showAllocParam() throws InterruptedException {

        // -Dio.netty.allocator.type=unpooled   -Dio.netty.allocator.type=pooled
        // -Dio.netty.noPreferDirect=true      -Dio.netty.noPreferDirect=false     DIRECT_BUFFER_PREFERRED
        EmbeddedChannel channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter() {

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                ByteBufAllocator allocator = ctx.alloc();

                logger.info("{}", allocator);

                ctx.channel().alloc();
                ByteBuf buffer = allocator.buffer();
                logger.info("{}", buffer);
//                ctx.fireChannelRead(msg);
            }
        });

        channel.writeInbound(new Object());

        Thread.sleep(Integer.MAX_VALUE);
    }
}
