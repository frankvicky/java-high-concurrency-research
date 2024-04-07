package frankvicky.cc.nio.reactor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class EchoClient {
    private static final Logger logger = LoggerFactory.getLogger(EchoClient.class);

    public void start() throws IOException {
        InetSocketAddress address = new InetSocketAddress(18787);
        SocketChannel socketChannel = SocketChannel.open(address);
        logger.info("client connected to {}", address);
        socketChannel.configureBlocking(false);
        socketChannel.setOption(StandardSocketOptions.TCP_NODELAY, true);

        //不斷的自旋、等待連接完成，或者做一些其他的事情
        while (!socketChannel.finishConnect()) {
            logger.info("waiting for connection");
        }

        logger.info("connection established");

        //啟動接受線程
        Processor processor = new Processor(socketChannel);
        Commander commander = new Commander(processor);
        new Thread(commander).start();
        new Thread(processor).start();
    }

    static class Commander implements Runnable {
        Processor processor;

        Commander(Processor processor) throws IOException {
            //Reactor初始化
            this.processor = processor;
        }

        public void run() {
            while (!Thread.interrupted()) {
                ByteBuffer buffer = processor.sendBuffer;
                Scanner scanner = new Scanner(System.in);

                while (processor.hasData.get()) {
                    logger.info("還有消息沒有發送完，請稍等");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                logger.info("請輸入發送內容:");

                if (scanner.hasNext()) {
                    String next = scanner.next();
                    buffer.put((LocalDate.now() + " >>" + next).getBytes());
                    processor.hasData.set(true);
                }
            }
        }
    }

    static class Processor implements Runnable {
        ByteBuffer sendBuffer = ByteBuffer.allocate(1024);
        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
        protected AtomicBoolean hasData = new AtomicBoolean(false);
        final Selector selector;
        final SocketChannel channel;

        Processor(SocketChannel channel) throws IOException {
            //Reactor初始化
            selector = Selector.open();
            this.channel = channel;
            channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }

        public void run() {
            try {
                while (!Thread.interrupted()) {
                    selector.select();
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> keysIterator = selectedKeys.iterator();

                    while (keysIterator.hasNext()) {
                        SelectionKey selectedKey = keysIterator.next();
                        if (selectedKey.isWritable()) {
                            if (hasData.get()) {
                                SocketChannel socketChannel = (SocketChannel) selectedKey.channel();
                                sendBuffer.flip();
                                socketChannel.write(sendBuffer);
                                sendBuffer.clear();
                                hasData.set(false);
                            }
                        }

                        if (selectedKey.isReadable()) {
                            SocketChannel socketChannel = (SocketChannel) selectedKey.channel();
                            int length = 0;
                            while ((length = socketChannel.read(readBuffer)) > 0) {
                                readBuffer.flip();
                                logger.info("server echo:" + new String(readBuffer.array(), 0, length));
                                readBuffer.clear();
                            }
                        }
                        //處理結束了, 這裡不能關閉select key，需要重覆使用
                        //selectionKey.cancel();
                    }
                    selectedKeys.clear();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new EchoClient().start();
    }
}

