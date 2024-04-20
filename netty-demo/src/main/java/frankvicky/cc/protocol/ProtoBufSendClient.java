package frankvicky.cc.protocol;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProtoBufSendClient {
    private final static Logger logger = LoggerFactory.getLogger(ProtoBufSendClient.class);
    private final static String content = "瘋狂創客圈：高效能學習社群!";
    private final int serverPort;
    private final String serverIp;
    Bootstrap bootstrap = new Bootstrap();

    public ProtoBufSendClient(String ip, int port) {
        this.serverPort = port;
        this.serverIp = ip;
    }

    public void runClient() {
        EventLoopGroup workerLoopGroup = new NioEventLoopGroup();

        try {
            bootstrap.group(workerLoopGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.remoteAddress(serverIp, serverPort);
            bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel ch) throws Exception {
                    // 客戶端 channel pipeline 新增 2 個 handler
                    ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                    ch.pipeline().addLast(new ProtobufEncoder());
                }
            });

            ChannelFuture channelFuture = bootstrap.connect();
            channelFuture.addListener((ChannelFuture futureListener) -> {
                if (futureListener.isSuccess()) {
                    logger.info("EchoClient 客戶端連接成功!");
                } else {
                    logger.info("EchoClient 客戶端連接失敗!");
                }
            });

            // 阻塞,直到連接完成
            channelFuture.sync();
            Channel channel = channelFuture.channel();

            //發送 Protobuf 物件
            for (int i = 0; i < 1000; i++) {
                MsgProtos.Msg msg = build(i, i + "->" + content);
                channel.writeAndFlush(msg);
                logger.info("發送報文數：{}", i);
            }
            channel.flush();

            // 等待通道關閉的異步任務結束
            // 服務監聽通道將一直等待通道關閉的異步任務結束
            ChannelFuture closeFuture = channel.closeFuture();
            closeFuture.sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 優雅關閉EventLoopGroup，
            // 釋放掉所有資源包含創建的線程
            workerLoopGroup.shutdownGracefully();
        }
    }

    //建立ProtoBuf物件
    public MsgProtos.Msg build(int id, String content) {
        MsgProtos.Msg.Builder builder = MsgProtos.Msg.newBuilder();
        builder.setId(id);
        builder.setContent(content);
        return builder.build();
    }

    public static void main(String[] args) throws InterruptedException {
        new ProtoBufSendClient("127.0.0.1", 18787).runClient();
    }
}

