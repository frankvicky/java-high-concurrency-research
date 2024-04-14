package frankvicky.cc.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// 與 InBound Pipeline 不同的是，OutBound Pipeline 的執行順序是註冊順序的相反
public class OutPipeline {
    private static final Logger logger = LoggerFactory.getLogger(OutPipeline.class);

    public class SimpleOutHandlerA extends ChannelOutboundHandlerAdapter {
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            logger.info("OutBoundHandler A is Invoked: write");

            super.write(ctx, msg, promise);
        }

        @Override
        public void flush(ChannelHandlerContext ctx) throws Exception {
            logger.info("OutBoundHandler A is Invoked: flush");
            ctx.flush();
        }
    }

    public class SimpleOutHandlerB extends ChannelOutboundHandlerAdapter {
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            logger.info("OutBoundHandler B is Invoked: write");
            super.write(ctx, msg, promise);
        }

        @Override
        public void flush(ChannelHandlerContext ctx) throws Exception {
            logger.info("OutBoundHandler B is Invoked: flush");
            ctx.flush();
        }

    }

    public class SimpleOutHandlerC extends ChannelOutboundHandlerAdapter {
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            logger.info("OutBoundHandler C is Invoked: write");
            super.write(ctx, msg, promise);
        }

        @Override
        public void flush(ChannelHandlerContext ctx) throws Exception {
            logger.info("OutBoundHandler C is Invoked: flush");
            ctx.flush();
        }

    }

    @Test
    public void testPipelineOutBound() {
        ChannelInitializer channelInitializer = new ChannelInitializer<EmbeddedChannel>() {
            protected void initChannel(EmbeddedChannel channel) {
                channel.pipeline().addLast(new SimpleOutHandlerA());
                channel.pipeline().addLast(new SimpleOutHandlerB());
                channel.pipeline().addLast(new SimpleOutHandlerC());
            }
        };
        EmbeddedChannel channel = new EmbeddedChannel(channelInitializer);
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(1);
        channel.writeAndFlush(buffer);

        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public class SimpleOutHandlerB2 extends ChannelOutboundHandlerAdapter {
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            logger.info("OutBoundHandler B2 is Invoked: write");
            // 如果不呼叫父類別 的 write 或是 ChannelHandlerContext 的 write 方法，會造成 pipeline 中斷
            // 為什麼會如此可以查看 AbstractChannelHandlerContext 的 write 方法
            super.write(ctx, msg, promise);
        }
    }

    @Test
    public void testPipelineOutBoundCutting() {
        ChannelInitializer channelInitializer = new ChannelInitializer<EmbeddedChannel>() {
            protected void initChannel(EmbeddedChannel channel) {
                channel.pipeline().addLast(new SimpleOutHandlerA());
                channel.pipeline().addLast(new SimpleOutHandlerB2());
                channel.pipeline().addLast("SimpleOutHandlerC", new SimpleOutHandlerC());
            }
        };
        EmbeddedChannel channel = new EmbeddedChannel(channelInitializer);
        ByteBuf buf = Unpooled.buffer();
        buf.writeInt(1);
        channel.writeOutbound(buf);

//        channel.writeAndFlush(buf);


        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
