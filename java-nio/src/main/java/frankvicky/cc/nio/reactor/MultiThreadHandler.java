package frankvicky.cc.nio.reactor;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class MultiThreadHandler implements Runnable {
    final SocketChannel channel;
    final SelectionKey selectionKey;
    ByteBuffer input = ByteBuffer.allocate(1024);
    ByteBuffer output = ByteBuffer.allocate(1024);
    static final int READING = 0, SENDING = 1;
    int state = READING;

    ExecutorService pool = Executors.newFixedThreadPool(2);
    static final int PROCESSING = 3;

    MultiThreadHandler(Selector selector, SocketChannel c) throws IOException {
        channel = c;
        c.configureBlocking(false);
        selectionKey = channel.register(selector, 0);

        // 將 Handler 作為 callback 物件
        selectionKey.attach(this);
        //第二步，註冊Read就緒事件
        selectionKey.interestOps(SelectionKey.OP_READ);
        selector.wakeup();
    }
    boolean inputIsComplete() {
        // ...
        return true;
    }
    boolean outputIsComplete() {
        //...
        return true;
    }
    void process() {
        //...
        return;
    }
    public void run() {
        try {
            if (state == READING) {
                read();
            } else if (state == SENDING) {
                send();
            }
        } catch (IOException ex) { /* ... */ }
    }

    synchronized void read() throws IOException {
        // ...
        channel.read(input);
        if (inputIsComplete()) {
            state = PROCESSING;
            //使用 Thread pool異步執行
            pool.execute(new Processor());
        }
    }
    void send() throws IOException {
        channel.write(output);
        //write 完就結束了，關閉select key
        if (outputIsComplete()) {
            selectionKey.cancel();
        }
    }
    synchronized void processAndHandOff() {
        process();
        state = SENDING;
        // process完，開始等待 寫就緒
        selectionKey.interestOps(SelectionKey.OP_WRITE);
    }
    class Processor implements Runnable {
        public void run() {
            processAndHandOff();
        }
    }
}


