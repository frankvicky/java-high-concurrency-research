package frankvicky.cc.echoserver;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

@ChannelHandler.Sharable
public class NettyEchoClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(NettyEchoClientHandler.class);
    public static final NettyEchoClientHandler INSTANCE = new NettyEchoClientHandler();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        int length = byteBuf.readableBytes();
        byte[] arr = new byte[length];
        byteBuf.getBytes(0, arr);
        logger.info("client received: {} ", new String(arr, StandardCharsets.UTF_8));
        byteBuf.release();
//        ctx.fireChannelRead(in);
    }
}
