package frankvicky.cc.echoserver;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class NettyEchoServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(NettyEchoServerHandler.class);
    public static final NettyEchoServerHandler INSTANCE = new NettyEchoServerHandler();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;

        logger.info("Message type: {}", (byteBuf.hasArray() ? "HeapBuffer" : "DirectBuffer"));

        int length = byteBuf.readableBytes();
        byte[] byteArray = new byte[length];
        byteBuf.getBytes(0, byteArray);
        logger.info("Server received message: {}", new String(byteArray));
        logger.info("Before write back, message.refCnt: {}", byteBuf.refCnt());

        ChannelFuture channelFuture = ctx.writeAndFlush(msg);

        channelFuture.addListener(futureListener -> logger.info("After write back, message.refCnt: {}", byteBuf.refCnt()));
//        ctx.fireChannelRead(msg);
    }
}
