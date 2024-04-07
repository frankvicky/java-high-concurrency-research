package frankvicky.cc.nio.reactor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class EchoHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(EchoHandler.class);
    final SocketChannel socketChannel;
    final SelectionKey selectionKey;
    final ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
    static final int RECEIVING = 0, SENDING = 1;
    int state = RECEIVING;

    public EchoHandler(Selector selector, SocketChannel socketChannel) throws IOException {
        this.socketChannel = socketChannel;
        socketChannel.configureBlocking(false);
        // 把 socketChannel 註冊給 selector，並取得 selectionKey
        this.selectionKey = socketChannel.register(selector, 0);
        // 把 EchoHandler 作為 selectionKey 的附件，如此一來一個 selectionKey 對應一個 Handler 實例
        selectionKey.attach(this);
        // 註冊 Read 事件
        selectionKey.interestOps(SelectionKey.OP_READ);
        selector.wakeup();
    }

    @Override
    public void run() {
        try {
            if (state == SENDING) {
                // 處理寫就緒事件
                // 從 byteBuffer 讀取資料並寫入 SocketChannel
                socketChannel.write(byteBuffer);
                // 清空 byteBuffer 並反轉為寫模式
                byteBuffer.clear();
                // 將 selectionKey 改為監聽讀就緒事件
                selectionKey.interestOps(SelectionKey.OP_READ);
                state = RECEIVING;
            } else if (state == RECEIVING) {
                int length = 0;
                // 從 socketChannel 讀取資料並寫入 byteBuffer
                while ((length = socketChannel.read(byteBuffer)) > 0) {
                    byte[] bytes = new byte[length];
                    byteBuffer.get(bytes, 0, length);
                    logger.info(new String(bytes));
                    // 翻轉為讀模式
                    byteBuffer.flip();
                    // 將 selectionKey 改為監聽寫就緒事件
                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                    state = SENDING;
                }

                // 處理結束，但是這裡不能關閉 SelectionKey，因為要重複使用
                // selectionKey.cancel();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
