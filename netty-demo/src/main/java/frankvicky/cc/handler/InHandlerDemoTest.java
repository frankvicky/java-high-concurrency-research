package frankvicky.cc.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

public class InHandlerDemoTest {
    @Test
    public void testInHandlerLifeCircle() {
        InHandlerDemo inHandler = new InHandlerDemo();

        ChannelInitializer<EmbeddedChannel> channelInitializer = new ChannelInitializer<>() {
            protected void initChannel(EmbeddedChannel channel) {
                channel.pipeline()
                        .addLast(inHandler);
            }
        };

        EmbeddedChannel channel = new EmbeddedChannel(channelInitializer);
        ByteBuf buffer = Unpooled.buffer();
        // 模擬 inbound，向 embeddedChannel 寫入資料
        buffer.writeInt(1);
        channel.writeInbound(buffer);
        channel.flush();

        // 再模擬一次
        channel.writeInbound(buffer);
        channel.flush();
        channel.close();
    }
}
