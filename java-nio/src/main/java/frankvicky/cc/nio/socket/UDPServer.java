package frankvicky.cc.nio.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

public class UDPServer {
    private static final Logger logger = LoggerFactory.getLogger(UDPServer.class);

    public void receive() throws IOException {
        DatagramChannel datagramChannel = DatagramChannel.open();
        datagramChannel.configureBlocking(false);
        // 綁定監聽地址
        datagramChannel.bind(new InetSocketAddress("127.0.0.1", 18786));

        // 開啟一個 Channel Selector
        Selector selector = Selector.open();
        // 把 datagramChannel 註冊給 Selector, 並指定想要監聽的 io 事件

        // SelectionKey 代表 Channel IO 的就緒狀態
        // SelectionKey 本身是透過 bitwise 實現，因此可以透過 | 運算子實現多重監聽
        // int interestedIOEvent = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
        datagramChannel.register(selector, SelectionKey.OP_READ);
        // 並非所有 Channel 都可以註冊給 Selector，除非 Channel 有繼承 AbstractSelectableChannel

        while (selector.select() > 0) {
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            while (keys.hasNext()) {
                SelectionKey key = keys.next();

                // 是否有可讀 io 事件
                if (key.isReadable()) {
                    // 從 datagramChannel 接收訊息並寫入 buffer
                    SocketAddress client = datagramChannel.receive(buffer);
                    buffer.flip();
                    buffer.clear();
                }
            }

            keys.remove();
        }

        selector.close();
        datagramChannel.close();
    }

    public static void main(String[] args) throws IOException {
        new UDPServer().receive();
    }
}
