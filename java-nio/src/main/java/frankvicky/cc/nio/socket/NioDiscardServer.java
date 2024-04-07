package frankvicky.cc.nio.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NioDiscardServer {
    private static final Logger logger = LoggerFactory.getLogger(NioDiscardServer.class);

    public static void startServer() throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(18787));

        logger.info("Server started on port 18787");

        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        // select 返回值的意思：從上次 select 到這次 select，有多少 Channel 發生了 IO 事件
        // 註：會被 select 到事件來自有註冊的 Channel ，且事件本身也有被註冊
        // select 方法是阻塞調用，因此直到有一個已註冊 Channel 發生被註冊的 IO 事件才會有返回值
        // selectNow 則是非阻塞
        while (selector.select() > 0) {
            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();

            while (selectedKeys.hasNext()) {
                SelectionKey selectedKey = selectedKeys.next();

                if (selectedKey.isAcceptable()) {
                    // 就緒事件代表有 Socket 連線成功
                    // ServerSocket 透過 accept 方法接受 Socket 連線並建立 SocketChannel
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    // 將新建立的 SocketChannel 註冊給 Selector 並監聽讀就緒事件
                    socketChannel.register(selector, SelectionKey.OP_READ);
                } else if (selectedKey.isReadable()) {
                    // ServerSocketChannel 只支援 Accept 事件，因此讀就緒事件必然是由 SocketChannel 發送
                    SocketChannel socketChannel = (SocketChannel) selectedKey.channel();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

                    int length = 0;
                    // 從 socketChannel 讀取資料然後寫入 byteBuffer
                    while (((length = socketChannel.read(byteBuffer))) > 0) {
                        // 切換為讀模式
                        byteBuffer.flip();

                        // 建立一個與讀取到資料長度相同的 byte 陣列
                        byte[] bytes = new byte[length];
                        // 將讀取到資料從 byteBuffer 取出，寫入 byte 陣列
                        byteBuffer.get(bytes, 0, length);
                        logger.info(new String(bytes), 0, length);
                        // 清空 buffer 並翻轉為寫模式，為下次寫入做準備
                        byteBuffer.clear();
                    }
                    socketChannel.close();
                }
                selectedKeys.remove();
            }
        }

        serverSocketChannel.close();
    }

    public static void main(String[] args) throws IOException {
        startServer();
    }
}
