package frankvicky.cc.basic;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyDiscardServer {
    private static final Logger logger = LoggerFactory.getLogger(NettyDiscardServer.class);
    private final int serverPort;
    // ServerBootstrap 是組裝和整合器，把不同職責的 Netty Component 組裝在一起
    ServerBootstrap bootstrap = new ServerBootstrap();

    public NettyDiscardServer(int serverPort) {
        this.serverPort = serverPort;
    }

    public void runServer() {
        // 以之前實作的 MultiThread Reactor 來說， NioEventLoopGroup 就是對應 Reactor
        NioEventLoopGroup bossLoopGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerLoopGroup = new NioEventLoopGroup();

        try {
            // 設定 reactor polling group
            bootstrap.group(bossLoopGroup, workerLoopGroup);
            // 設置 Nio 類型的 Channel
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.localAddress(serverPort);
            // 設置 Channel 參數
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);

            // 組裝 child Channel 的 pipeline
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                // 如果有新連接抵達時會建立一個 channel
                protected void initChannel(SocketChannel channel) {
                    // pipeline 的任務：管理 pipeline 中的 handler
                    // 這裡是向 Child Channel 的 pipeline 加入一個 handler
                    channel.pipeline()
                            .addLast(new NettyDiscardHandler());
                    // 這裏 NettyDiscardHandler 對應的是 MultiThreadReactor 中 SelectionKey 的 Handler
                }
            });

            // 綁定 server
            // sunc 是同步阻塞方法，直到綁定成功才會回傳
            ChannelFuture channelFuture = bootstrap.bind().sync();
            logger.info("Server started on port {}", serverPort);
            // 等待 close Channel 的 asyn 任務結束
            // server 會一直監聽 channel 直到 close channel 完成
            ChannelFuture closeFuture = channelFuture.channel().closeFuture();
            closeFuture.sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workerLoopGroup.shutdownGracefully();
            bossLoopGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new NettyDiscardServer(18787).runServer();
    }
}
