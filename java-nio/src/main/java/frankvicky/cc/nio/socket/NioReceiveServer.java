package frankvicky.cc.nio.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class NioReceiveServer {
    private static final Logger logger = LoggerFactory.getLogger(NioReceiveServer.class);
    private static final String RECEIVE_PATH = "java-nio/src/main/resources/FileChannelExample/receive/";
    private final Charset charset = StandardCharsets.UTF_8;

    static class Session {
        int step = 1;
        String fileName = null;
        long fileLength;
        int fileNameLength;
        long startTime;
        InetSocketAddress remoteAddress;
        FileChannel fileChannel;
        long receiveLength;
        public boolean isFinished() {
            return receiveLength >= fileLength;
        }
    }

    private final ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
    //使用 Map 儲存每個 session 傳輸，當 OP_READ 事件產生時，代表 channel 可讀，就可以根據 channel 找到對應的對象
    Map<SelectableChannel, Session> channelToSession = new HashMap<>();

    public void startServer() throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        ServerSocket serverSocket = serverSocketChannel.socket();
        serverSocketChannel.configureBlocking(false);

        InetSocketAddress address = new InetSocketAddress(18787);
        serverSocket.bind(address);
        // 將 channel 註冊到 selector 上，並註冊 Accept 事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        logger.info("ServerChannel is listening on port 18787...");

        // 輪詢是否有已註冊的 IO 就緒事件產生
        while (selector.select() > 0) {
            if (null == selector.selectedKeys())
                continue;

            Iterator<SelectionKey> keysIterator = selector.selectedKeys().iterator();
            while (keysIterator.hasNext()) {
                SelectionKey selectedKey = keysIterator.next();
                if (null == selectedKey)
                    continue;

                if (selectedKey.isAcceptable()) {
                    // 若接收的事件是 Accept 事件，就呼叫 accept 方法建立 socketChannel
                    ServerSocketChannel server = (ServerSocketChannel) selectedKey.channel();
                    SocketChannel socketChannel = server.accept();
                    if (socketChannel == null) continue;
                    socketChannel.configureBlocking(false);
                    socketChannel.setOption(StandardSocketOptions.TCP_NODELAY, true);
                    // 將 socketChannel 註冊到 selector
                    socketChannel.register(selector, SelectionKey.OP_READ);

                    // 餘下為業務處理
                    Session session = new Session();
                    session.remoteAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
                    channelToSession.put(socketChannel, session);
                    logger.info(socketChannel.getRemoteAddress() + " has connected");
                } else if (selectedKey.isReadable()) {
                    handleData(selectedKey);
                }
                // NIO 的特點只會累加，已選擇的鍵的集合不會刪除
                // 如果不刪除，下一次又會被 select 方法選中
                keysIterator.remove();
            }
        }
    }

    private void handleData(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        int num = 0;
        Session session = channelToSession.get(key.channel());

        byteBuffer.clear();
        while ((num = socketChannel.read(byteBuffer)) > 0) {
            logger.info("received byte length = " + num);
            //切換到讀模式
            byteBuffer.flip();
            process(session, byteBuffer);
            byteBuffer.clear();
        }

    }

    private void process(Session session, ByteBuffer buffer) {
        while (length(buffer) > 0) {   // 客戶端發送過來的，首先處理文件名長度
            if (1 == session.step) {
                int fileNameLengthByteLen = length(buffer);
                System.out.println("讀取文件名稱長度之前，可讀取的字節數 = " + fileNameLengthByteLen);
                System.out.println("讀取文件名稱長度之前，buffer.remaining() = " + buffer.remaining());
                System.out.println("讀取文件名稱長度之前，buffer.capacity() = " + buffer.capacity());
                System.out.println("讀取文件名稱長度之前，buffer.limit() = " + buffer.limit());
                System.out.println("讀取文件名稱長度之前，buffer.position() = " + buffer.position());
                if (length(buffer) < 4) {
                    logger.info("出現半包問題，需要更新穎的複製的拆包方案");
                    throw new RuntimeException("出現半包問題，需要更新穎的複製的拆包方案");
                }

                session.fileNameLength = buffer.getInt();
                System.out.println("讀取文件名稱長度之後，buffer.remaining() = " + buffer.remaining());
                System.out.println("讀取文件名稱長度 = " + session.fileNameLength);
                session.step = 2;
            } else if (2 == session.step) {
                logger.info("step 2");

                if (length(buffer) < session.fileNameLength) {
                    logger.info("出現半包問題，需要更新穎的複製的拆包方案");
                    throw new RuntimeException("出現半包問題，需要更新穎的複製的拆包方案");
                }

                byte[] fileNameBytes = new byte[session.fileNameLength];

                //讀取文件名稱
                buffer.get(fileNameBytes);

                // 文件名稱
                String fileName = new String(fileNameBytes, charset);
                System.out.println("讀取文件名稱 = " + fileName);
                File directory = new File(RECEIVE_PATH);

                if (!directory.exists()) {
                    directory.mkdir();
                }

                logger.info("NIO  傳輸目標 dir:", directory);

                session.fileName = fileName;
                String fullName = directory.getAbsolutePath() + File.separatorChar + fileName;
                logger.info("NIO  傳輸目標文件：", fullName);

                File file = new File(fullName.trim());

                try {
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    FileChannel fileChannel = new FileOutputStream(file).getChannel();
                    session.fileChannel = fileChannel;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                session.step = 3;
            } else if (3 == session.step) {
                logger.info("step 3");
                //客戶端發送過來的，首先處理文件內容長度
                if (length(buffer) < 4) {
                    logger.info("出現半包問題，需要更新穎的複製的拆包方案");
                    throw new RuntimeException("出現半包問題，需要更新穎的複製的拆包方案");
                }
                //獲取文件內容長度
                session.fileLength = buffer.getInt();
                System.out.println("讀取文件內容長度之後，buffer.remaining() = " + buffer.remaining());
                System.out.println("讀取文件內容長度 = " + session.fileLength);
                session.step = 4;
                session.startTime = System.currentTimeMillis();
            } else if (4 == session.step) {
                logger.info("step 4");
                //客戶端發送過來的，最後是文件內容
                session.receiveLength += length(buffer);
                // 寫入文件
                try {
                    session.fileChannel.write(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (session.isFinished()) {
                    finished(session);
                }
            }
        }
    }

    private void finished(Session session) {
        try {
            session.fileChannel.close();
            logger.info("上傳完畢");
            logger.debug("檔案接收成功,File Name：" + session.fileName);
            long endTime = System.currentTimeMillis();
            logger.debug("NIO IO 傳輸毫秒數：" + (endTime - session.startTime));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        NioReceiveServer server = new NioReceiveServer();
        server.startServer();
    }

    private static int length(ByteBuffer buffer) {
        logger.info(" >>>  buffer left：" + buffer.remaining());
        return buffer.remaining();
    }
}
