package frankvicky.cc.pipeline;

import frankvicky.cc.handler.OutHandlerDemo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InPipeline {
    private static final Logger logger = LoggerFactory.getLogger(InPipeline.class);

    static class SimpleInHandlerA extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            logger.info("InboundHandler A is Invoked: channelRead");
            super.channelRead(ctx, msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            logger.info("InboundHandler A is Invoked: channelReadComplete");

//            super.channelReadComplete(ctx);
            ctx.fireChannelReadComplete(); // Inbound 操作的傳播
        }


    }

    static class SimpleInHandlerB extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            logger.info("InboundHandler B is Invoked: channelRead");
            super.channelRead(ctx, msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            logger.info("InboundHandler B is Invoked: channelReadComplete");
            ctx.fireChannelReadComplete();
        }
    }


    static class SimpleInHandlerC extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            logger.info("InboundHandler C is Invoked: channelRead");
            super.channelRead(ctx, msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            logger.info("InboundHandler C is Invoked: channelReadComplete");
            ctx.fireChannelReadComplete();
        }
    }


    @Test
    public void testPipelineInBound() throws InterruptedException {
        ChannelInitializer channelInitializer = new ChannelInitializer<EmbeddedChannel>() {
            protected void initChannel(EmbeddedChannel channel) {
                channel.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                channel.pipeline().addLast(new SimpleInHandlerA());
                channel.pipeline().addLast(new SimpleInHandlerB());
                channel.pipeline().addLast(new SimpleInHandlerC());

            }
        };
        EmbeddedChannel channel = new EmbeddedChannel(channelInitializer);
        ByteBuf buf = Unpooled.buffer();
        buf.writeInt(1);
        // 向 channel 寫入一個 inbound 資料
        channel.writeInbound(buf);
        Thread.sleep(Integer.MAX_VALUE);
    }


    static class SimpleInHandlerB2 extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            logger.info("InBoundHandler B2 is Invoked: channelRead");
            // 如果不呼叫父類別 的 channelRead 或是 ChannelHandlerContext 的 fireChannelRead 方法，會造成 pipeline 中斷
            // 為什麼會如此可以查看 AbstractChannelHandlerContext 的 fireChannelRead 方法

            // super.channelRead(ctx, msg);
            //  ctx.fireChannelRead(msg);
        }


        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            logger.info("InBoundHandler B2 is Invoked: channelReadComplete");
            ctx.fireChannelReadComplete();
        }
    }

    //测试流水线的截断
    @Test
    public void testPipelineCutting() {
        ChannelInitializer channelInitializer = new ChannelInitializer<EmbeddedChannel>() {
            protected void initChannel(EmbeddedChannel channel) {
                channel.pipeline().addLast(new SimpleInHandlerA());
                channel.pipeline().addLast(new SimpleInHandlerB2());
                channel.pipeline().addLast(new SimpleInHandlerC());

            }
        };
        EmbeddedChannel channel = new EmbeddedChannel(channelInitializer);
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(1);
        channel.writeInbound(buffer);
        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    static class SimpleInHandlerB3 extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            logger.info("InBoundHandler B3 is Invoked");


            ByteBuf buffer = Unpooled.buffer();
            buffer.writeInt(10);

            // ctx.channel().writeAndFlush(buffer);
            // ctx.pipeline().writeAndFlush(buffer);

            ctx.writeAndFlush(buffer);
            // super.channelRead(ctx, msg);
            //  ctx.fireChannelRead(msg);
        }

    }

    @Test
    public void testMultiplyOutput() {
        OutHandlerDemo outHandlerDemo = new OutHandlerDemo();
        ChannelInitializer channelInitializer = new ChannelInitializer<EmbeddedChannel>() {
            protected void initChannel(EmbeddedChannel channel) {
                channel.pipeline().addLast(new SimpleInHandlerA());
                channel.pipeline().addLast(outHandlerDemo);
                channel.pipeline().addLast(new SimpleInHandlerB3());
                channel.pipeline().addLast(new SimpleInHandlerC());
                channel.pipeline().addLast(outHandlerDemo);

            }
        };

        EmbeddedChannel channel = new EmbeddedChannel(channelInitializer);
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(1);
        channel.writeInbound(buffer);
        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
