package frankvicky.cc.protocol;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProtoBufServer {
    private static final Logger logger = LoggerFactory.getLogger(ProtoBufServer.class);
    private final int serverPort;
    ServerBootstrap bootstrap = new ServerBootstrap();

    public ProtoBufServer(int port) {
        this.serverPort = port;
    }

    public void runServer() {
        //建立 NioEventLoopGroup(MultiThreadReactor)
        EventLoopGroup bossLoopGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerLoopGroup = new NioEventLoopGroup();

        try {
            bootstrap.group(bossLoopGroup, workerLoopGroup);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.localAddress(serverPort);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                // 有連線到達時會創建一個channel
                protected void initChannel(SocketChannel ch) throws Exception {
                    // pipeline 管理 child Channel 中的 Handler
                    // 向 child Channel pipeline 新增 3 個 handler

                    // protobufDecoder 僅僅負責編碼，並不支援讀半包，所以在之前，一定要有讀半包的 handler
                    // 有三種方式可以選擇：
                    // 使用 netty 提供 ProtobufVarint32FrameDecoder
                    // 繼承 netty 提供的通用半包處理器 LengthFieldBasedFrameDecoder
                    // 繼承 ByteToMessageDecoder 類，自己處理半包

                    // 半包的處理
                    ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                    // 需要解碼的目標類
                    ch.pipeline().addLast(new ProtobufDecoder(MsgProtos.Msg.getDefaultInstance()));
                    ch.pipeline().addLast(new ProtobufBussinessHandler());
                }
            });
            // 通過調用 sync 方法阻塞直到綁定成功
            ChannelFuture channelFuture = bootstrap.bind().sync();
            logger.info("服務器啟動成功，監聽端口: " + channelFuture.channel().localAddress());

            // 等待通道關閉的異步任務結束
            // 服務監聽通道會一直等待通道關閉的異步任務結束
            ChannelFuture closeFuture = channelFuture.channel().closeFuture();
            closeFuture.sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 8 優雅關閉EventLoopGroup，
            // 釋放掉所有資源包括創建的線程
            workerLoopGroup.shutdownGracefully();
            bossLoopGroup.shutdownGracefully();
        }

    }

    //服務端業務處理器
    static class ProtobufBussinessHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            MsgProtos.Msg protoMsg = (MsgProtos.Msg) msg;
            //經過pipeline的各個decoder，到此Person類型已經可以斷定
            logger.info("收到一個 MsgProtos.Msg 數據包 =》");
            logger.info("protoMsg.getId():= {}", protoMsg.getId());
            logger.info("protoMsg.getContent():= {}", protoMsg.getContent());
        }
    }


    public static void main(String[] args) throws InterruptedException {
        new ProtoBufServer(18787).runServer();
    }
}