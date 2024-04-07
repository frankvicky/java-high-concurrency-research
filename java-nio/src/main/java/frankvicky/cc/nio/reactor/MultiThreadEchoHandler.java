package frankvicky.cc.nio.reactor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class MultiThreadEchoHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(MultiThreadEchoHandler.class);
    final SocketChannel socketChannel;
    final SelectionKey selectionKey;
    final ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
    static final int RECEIVING = 0, SENDING = 1;
    int state = RECEIVING;
    static ExecutorService pool = Executors.newFixedThreadPool(4);

    MultiThreadEchoHandler(Selector selector, SocketChannel socketChannel) throws IOException {
        this.socketChannel = socketChannel;
        socketChannel.configureBlocking(false);
        socketChannel.setOption(StandardSocketOptions.TCP_NODELAY, true);
        selectionKey = socketChannel.register(selector, 0);
        //將 Handler 作為 selectionKey 的附件，方便事件 dispatch
        selectionKey.attach(this);
        //向 selectionKey 註冊讀就緒事件
        selectionKey.interestOps(SelectionKey.OP_READ);
        //喚醒查詢執行緒，使得OP_READ生效
        selector.wakeup();
    }

    public void run() {
        //異步任務，在獨立的 Thread Pool 中執行
        //提交數據傳輸任務到 Thread Pool
        //使得IO處理不在IO事件輪詢線程中執行，在獨立的 Thread Pool 中執行
        pool.execute(new AsyncTask());
    }

    //異步任務，不在 Reactor Thread 中執行
    //數據傳輸與業務處理任務，不在IO事件輪詢線程中執行，在獨立的 Thread Pool 中執行
    public synchronized void asyncRun() {
        try {
            if (state == SENDING) {
                socketChannel.write(byteBuffer);
                byteBuffer.clear();
                selectionKey.interestOps(SelectionKey.OP_READ);
                state = RECEIVING;
            } else if (state == RECEIVING) {
                int length = 0;
                while ((length = socketChannel.read(byteBuffer)) > 0) {
                    logger.info(new String(byteBuffer.array(), 0, length));
                }
                byteBuffer.flip();
                selectionKey.interestOps(SelectionKey.OP_WRITE);
                state = SENDING;
            }
            //處理結束了, 這裡不能關閉select key，需要重複使用
            //sk.cancel();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    class AsyncTask implements Runnable {
        public void run() {
            MultiThreadEchoHandler.this.asyncRun();
        }
    }
}

