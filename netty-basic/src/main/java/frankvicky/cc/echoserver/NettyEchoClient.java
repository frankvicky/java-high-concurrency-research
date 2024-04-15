package frankvicky.cc.echoserver;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Scanner;

public class NettyEchoClient {
    private final static Logger logger = LoggerFactory.getLogger(NettyEchoClient.class);
    private final String targetIp;
    private final int port;
    Bootstrap bootstrap = new Bootstrap();

    public NettyEchoClient(String ip, int port) {
        this.targetIp = ip;
        this.port = port;
    }

    public void runClient() {
        NioEventLoopGroup workerLoopGroup = new NioEventLoopGroup();

        try {
            bootstrap.group(workerLoopGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.remoteAddress(targetIp, port);
            bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);

            bootstrap.handler(new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel channel) throws Exception {
                    channel.pipeline().addLast(NettyEchoClientHandler.INSTANCE);
                    // todo NettyEchoClientHandler
                }
            });

            ChannelFuture channelFuture = null;
            boolean connected = false;

            while (!connected) {
                channelFuture = bootstrap.connect();
                channelFuture.addListener(futureListener -> {
                    if (futureListener.isSuccess()) {
                        logger.info("Client connected");
                    } else {
                        logger.info("Client connection failed");
                    }
                });

//                channelFuture.sync()
                channelFuture.awaitUninterruptibly();

                if (channelFuture.isCancelled()) {
                    logger.info("Client disconnected");
                    return;
                } else if (channelFuture.isSuccess()) {
                    connected = true;
                }
            }

            Channel channel = channelFuture.channel();
            Scanner scanner = new Scanner(System.in);
            logger.info("Please enter your message:");

            GenericFutureListener genericFutureListener = new GenericFutureListener() {

                @Override
                public void operationComplete(Future future) throws Exception {
                    if (future.isSuccess()) {
                        logger.info("Message sent");
                    } else {
                        logger.info("Message send failed");
                    }
                }
            };

            while (scanner.hasNext()) {
                String next = scanner.next();
                byte[] bytes = (LocalDate.now() + " >>" + next).getBytes(StandardCharsets.UTF_8);
                // 發送 byteBuf
                ByteBuf buffer = channel.alloc().buffer();
                buffer.writeBytes(bytes);

//                channel.write(buffer);
//                buffer.retain();
//
//                channel.write(buffer);
//                buffer.retain();

                 ChannelFuture writeAndFlushFuture = channel.writeAndFlush(buffer);
                writeAndFlushFuture.addListener(genericFutureListener);
                logger.info("请输入发送内容:");

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 优雅关闭EventLoopGroup，
            // 释放掉所有资源包括创建的线程
            workerLoopGroup.shutdownGracefully();
        }

    }

    public static void main(String[] args) throws InterruptedException {
        int port = 18787;
        String ip = "127.0.0.1";
        new NettyEchoClient(ip, port).runClient();
    }
}
