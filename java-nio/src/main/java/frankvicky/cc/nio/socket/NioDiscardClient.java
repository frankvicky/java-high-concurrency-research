package frankvicky.cc.nio.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NioDiscardClient {
    private static final Logger logger = LoggerFactory.getLogger(NioDiscardClient.class);

    public static void startClient() throws IOException {
        InetSocketAddress address = new InetSocketAddress("127.0.0.1", 18787);
        SocketChannel socketChannel = SocketChannel.open(address);
        socketChannel.configureBlocking(false);

        while (!socketChannel.finishConnect()) {
            logger.info("Try to connect to {}", address);
        }

        logger.info("Connected to {}", address);

        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        byteBuffer.put("Hello from the other side".getBytes());
        byteBuffer.flip();

        socketChannel.write(byteBuffer);
        socketChannel.shutdownOutput();
        socketChannel.close();
    }

    public static void main(String[] args) throws IOException {
        startClient();
    }
}
