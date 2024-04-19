package frankvicky.cc.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PipelineHotOperateTest {

    private static final Logger logger = LoggerFactory.getLogger(PipelineHotOperateTest.class);

    static class SimpleInHandlerA extends ChannelInboundHandlerAdapter {

        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            logger.info("InBoundHandler A is Invoked: channelRead");
            super.channelRead(ctx, msg);
            // 從 pipeline 刪除當前 handler
            ctx.pipeline().remove(this);
        }
    }

    static class SimpleInHandlerB extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            logger.info("InBoundHandler B is Invoked: channelRead");
            super.channelRead(ctx, msg);
        }
    }

    static class SimpleInHandlerC extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            logger.info("InBoundHandler C is Invoked: channelRead");
            super.channelRead(ctx, msg);
        }
    }

    @Test
    public void testPipelineHotOperating() {
        ChannelInitializer channelInitializer = new ChannelInitializer<EmbeddedChannel>() {
            protected void initChannel(EmbeddedChannel channel) {
                channel.pipeline().addLast(new SimpleInHandlerA());
                channel.pipeline().addLast(new SimpleInHandlerB());
                channel.pipeline().addLast(new SimpleInHandlerC());
            }
        };

        EmbeddedChannel channel = new EmbeddedChannel(channelInitializer);
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(1);

        channel.writeInbound(buffer);

        channel.writeInbound(buffer);

        channel.writeInbound(buffer);

        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
