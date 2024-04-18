package frankvicky.cc.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// 這個 class 的目的在於列印所有 ChannelInboundHandler 的方法被呼叫的瞬間
// 以此了解 Channel 在 Netty 的生命週期
// 除了 ChannelRead 和 ChannelReadCompleted 以外，其餘都是生命週期方法
public class InHandlerDemo extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(InHandlerDemo.class);

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // 當 Handler 被加入 pipeline，就會回呼 handlerAdded
        // 也就是在 channel.pipeline().addLast(handler) 之後
        logger.info("Invoked: handlerAdded");
        super.handlerAdded(ctx);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        // 當 channel 成功綁定一個 NioEventLoop (Reactor) 之後，回呼 channelRegistered
        logger.info("Invoked: channelRegistered");
        super.channelRegistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 這裡的 channel active 是指當所有 handler 的非同步註冊任務完成，並且與 NioEventLoop(Reactor) 的非同步綁定任務完成之後
        // 上述 active 的條件被滿足之後，就會回呼 channelActive
        logger.info("Invoked: channelActive");
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 有 data inbound，且 channel 發出讀就緒事件，此時 pipeline 會啟動 inbound 的處理流程
        // 由前向後，InboundHandler 的 channelRead 方法會依順序被回呼
        logger.info("Invoked: channelRead");
        super.channelRead(ctx, msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // pipeline 完成 inbound 處理後，會由前向後，依順序回呼每個 inboundHandler 的 channelReadComplete 方法，代表資料讀取完畢
        logger.info("Invoked: channelReadComplete");
        super.channelReadComplete(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 當 channel 的底層連接已經不是 ESTABLISH 狀態或是底層連接已經被關閉，會首先回呼所有 handler 的 channelInactive 方法
        logger.info("Invoked: channelInactive");
        super.channelInactive(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        // channel 和 NioEventLoop(Reactor) 解除綁定之後，移除掉對這條 channel 的事件處理之後，會回呼所有 handler 的 channelUnregistered
        logger.info("Invoked: channelUnregistered");
        super.channelUnregistered(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        // 最後，Netty 會移除掉 channel 上所有的 handler，並且回呼所有 handler 的 handlerRemoved
        logger.info("Invoked: handlerRemoved");
        super.handlerRemoved(ctx);
    }
}
