package frankvicky.cc.basic;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Netty 的 Handler 需要處理多種 IO Event（例如讀就緒、寫就緒）
// 對應不同的 IO Event，Netty 提供了一系列基礎方法，這些方法都已經封裝好，應用程式直接繼承或實現即可
public class NettyDiscardHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(NettyDiscardHandler.class);

    // channelRead 方法是將 Netty Buffer 裡的資料讀取出來
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // Netty ByteBuf 對應 Java NIO ByteBuffer，相較起來性能更好也更易於使用
        ByteBuf in = (ByteBuf) msg;

        try {
            logger.info("Received msg: ");
            while (in.isReadable()) {
                System.out.println((char) in.readByte());
            }

            System.out.println();
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
