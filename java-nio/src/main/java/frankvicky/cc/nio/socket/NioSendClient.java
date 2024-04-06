package frankvicky.cc.nio.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class NioSendClient {

    private static final Logger logger = LoggerFactory.getLogger(NioSendClient.class);
    private final Charset charset = StandardCharsets.UTF_8;

    public void sendFile() {
        try {
            String srcPath = "java-nio/src/main/resources/FileChannelExample/origin.txt";

            File file = new File(srcPath);

            if (!file.exists()) {
                logger.error("File is not exist");
                logger.debug("srcPath = " + srcPath);
                return;
            }

            FileChannel fileChannel = new FileInputStream(file).getChannel();
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.setOption(StandardSocketOptions.TCP_NODELAY, true);
            // 根據Java的NIO文檔，configureBlocking(false)設置該通道為非阻塞模式，應該在connect()或finishConnect()之後進行設置。
            // 這是因為，若在呼叫connect()方法之前設置非阻塞，那麼connect()方法可能在連接完成之前就返回，而這不是期望的行為。
            socketChannel.socket().connect(new InetSocketAddress("127.0.0.1", 8787));
            socketChannel.configureBlocking(false); // 將 socketChannel 設置為 non-blocking 模式

            logger.debug("Attempt to connect server");

            while (!socketChannel.finishConnect()) {
                // finishConnect 方法會立即返回是否建立連線
                // 所以在真的與 Server 建立連線之前需要不斷迴圈
                logger.debug("Try to create connection ...");
            }

            ByteBuffer fileNameInByteBuffer = charset.encode(file.getName());
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int fileNameLength = fileNameInByteBuffer.remaining();
            buffer.clear();
            buffer.putInt(fileNameLength);
            // 切換為讀模式
            buffer.flip();
            // 從 buffer 讀取資料並寫入 socketChannel
            socketChannel.write(buffer);
            logger.info("Send length of file name to server: " + fileNameLength);

            socketChannel.write(fileNameInByteBuffer);
            logger.info("Send file name to server: " + file.getName());

            // 清空之前寫入的 fileNameLength
            buffer.clear();
            buffer.putInt((int) file.length());
            // 切換為讀模式
            buffer.flip();
            // 從 buffer 讀取資料並寫入 socketChannel
            socketChannel.write(buffer);
            logger.info("Send length of file to server: " + file.length());

            logger.debug("Start sending file...");
            int length = 0;
            long offset = 0;
            buffer.clear();

            // 從 fileChannel 讀取資料並寫入 buffer
            while ((length = fileChannel.read(buffer)) > 0) {
                buffer.flip();

                socketChannel.write(buffer);

                offset += length;
                logger.debug("| " + (100 * offset / file.length()) + "% |");
                buffer.clear();
            }

            // 暫停一分鐘
            Thread.sleep(60000);

            if (length == -1) {
                fileChannel.close();
                socketChannel.shutdownOutput();
                socketChannel.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        NioSendClient client = new NioSendClient();
        client.sendFile();
    }
}
