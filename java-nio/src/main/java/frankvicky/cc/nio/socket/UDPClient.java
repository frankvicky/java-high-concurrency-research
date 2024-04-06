package frankvicky.cc.nio.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Scanner;

public class UDPClient {

    private static final Logger logger = LoggerFactory.getLogger(UDPClient.class);

    public void send() throws IOException {
        DatagramChannel datagramChannel = DatagramChannel.open();
        datagramChannel.configureBlocking(false);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        Scanner scanner = new Scanner(System.in);

        logger.info("Client init successfully");
        logger.info("Please input your message");

        while (scanner.hasNext()) {
            String next = scanner.next();
            buffer.put((System.currentTimeMillis() + " >> " + next).getBytes());
            buffer.flip();
            // 從 buffer 讀取資料並寫入 datagramChannel 發送
            datagramChannel.send(buffer, new InetSocketAddress("127.0.0.1", 18787));
            buffer.clear();
        }
    }

    public static void main(String[] args) throws IOException {
        new UDPClient().send();
    }
}
