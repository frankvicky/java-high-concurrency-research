package frankvicky.cc.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

public class OutHandlerDemoTest {
    @Test
    public void testLifeCircle() {
        OutHandlerDemo outHandler = new OutHandlerDemo();
        ChannelInitializer<EmbeddedChannel> channelInitializer = new ChannelInitializer<>() {
            protected void initChannel(EmbeddedChannel channel) {
                channel.pipeline().addLast(outHandler);
            }
        };

        EmbeddedChannel channel = new EmbeddedChannel(channelInitializer);

        // 測試 OutBound write
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(1);

        ChannelFuture channelFuture = channel.pipeline().writeAndFlush(buffer);

        channelFuture.addListener((future) -> {
            if (future.isSuccess()) {
                System.out.println("write is finished");
            }
            channel.close();
        });

        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
