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
        // 以之前實作的 Reactor 來說， NioEventLoop 就是對應 Reactor
        // NioEventLoop 包含了一個不斷 Polling 的執行序和 Java NIO Selector
        // NioEventLoopGroup 就是一個 MultiThreadReactor
        // NioEventLoopGroup 建構子中有一個參數，用於指定內部的執行緒數量。
        // 在建構子中會依照該值在內部建立多個執行緒和 EventLoop (執行緒和 EventLoop 為 1:1)，用來進行多執行緒的 IO Event Polling 和 Dispatch
        NioEventLoopGroup bossLoopGroup = new NioEventLoopGroup(1);
        // 如果使用預設建構子或者傳入 0，EventLoopGroup 會使用預設值來建立執行緒，預設值是最大可用 CPU 處理器數量的兩倍
        NioEventLoopGroup workerLoopGroup = new NioEventLoopGroup();

        // 一般來說，一個 Reactor 會負責監聽連接和接受連接，另一個 Reactor 負責 IO Event Polling 和 Dispatch，兩者相互隔離。
        // 對應到 Netty Server App 中，就是設置兩個 NioEventLoopGroup，讓上方範例一樣

        try {
            // 設定 reactor polling group，注意第一個參數是 ParentGroup，第二個是 ChildGroup
            bootstrap.group(bossLoopGroup, workerLoopGroup);
            // 設置 Nio 類型的 Channel
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.localAddress(serverPort);
            // 設置 Parent Channel 參數，如果要設置 child Channel 則使用 childOption
            // 這裡開啟 TCP Heartbeat
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);

            // 組裝 child Channel 的 pipeline
            // 注意 ChannelInitializer 裡的泛形，他代表需要初始化的 Channel 類型，需要和前面 Bootstrap 設置的傳輸 Channel 類型保持一致
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

            // 為什麼沒有 Parent Channel pipeline ?
            // ParentChannel 是 NioServerSocketChannel，其業務邏輯固定，就是接收新連接然後創建 child Channel，因此由 Netty 自行裝配
            // 但如果有特殊邏輯需要在 Parent Channel 處理，可以使用 ServerBootstrap 的 Handler 方法

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
            // 釋放所有資源，包括 Reactor Thread
            workerLoopGroup.shutdownGracefully();
            bossLoopGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new NettyDiscardServer(18787).runServer();
    }
}
