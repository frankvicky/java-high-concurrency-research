package frankvicky.cc.nio.reactor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

// MultiThreadEchoServerReactor  裡面一開始先註冊 bossSelector （這裡專門用於處理 Accept 事件），然後附加 AcceptHandler 作為處理者。
// 此時 Selector 只有註冊一個通道和一種事件（ServerSocketChannel 和 OP_ACCEPT）。
// (其他 workSelectors 沒有註冊任何 Channel 和事件)

// 當有 SocketChannel 連線進來，就會去執行 bossReactor 的 run，裡面會 dispatch 被觸發的 IO 就緒事件
// 這個當下只有 OP_ACCEPT ，所以會呼叫 AcceptHandler 的 run 方法，裡面會透過呼叫 MultiThreadEchoHandler 的建構子建立這個類別的實例
// 也會把 SocketChannel  建立，並把這個 SocketChannel 和  OP_READ 和 OP_WRITE 註冊給指定編號的 Selector（兩個 workSelector 其中之一）。
// 至此 workReactor 的輪詢開始有意義，因為 workReactor 聚合的 workSelector 被註冊了 Channel 和 IO 就緒事件
class MultiThreadEchoServerReactor {
    private static final Logger logger = LoggerFactory.getLogger(MultiThreadEchoServerReactor.class);
    ServerSocketChannel serverSocketChannel;
    AtomicInteger next = new AtomicInteger(0);
    Selector bossSelector = null;
    Reactor bossReactor = null;
    //選擇器集合，導入多個選擇器
    Selector[] workSelectors = new Selector[2];
    //引入多個子反應器
    Reactor[] workReactors = null;

    MultiThreadEchoServerReactor() throws IOException {
        //初始化多個選擇器
        bossSelector = Selector.open();// 用於監聽新連接事件
        workSelectors[0] = Selector.open(); // 用於監聽read、write事件
        workSelectors[1] = Selector.open(); // 用於監聽read、write事件
        serverSocketChannel = ServerSocketChannel.open();
        InetSocketAddress address = new InetSocketAddress(18787);
        serverSocketChannel.socket().bind(address);
        serverSocketChannel.configureBlocking(false);//非阻塞
        //bossSelector,負責監視新連接事件, 將 serverSocketChannel 註冊到bossSelector
        SelectionKey selectionKey = serverSocketChannel.register(bossSelector, SelectionKey.OP_ACCEPT);
        //綁定Handler：新連接監控handler綁定到 SelectionKey
        selectionKey.attach(new AcceptorHandler());
        //bossReactor反應器，處理新連接的bossSelector
        bossReactor = new Reactor(bossSelector);
        //第一個子反應器，一子反應器負責一個worker選擇器
        Reactor workReactor1 = new Reactor(workSelectors[0]);
        //第二個子反應器，一子反應器負責一個worker選擇器
        Reactor workReactor2 = new Reactor(workSelectors[1]);
        workReactors = new Reactor[]{workReactor1, workReactor2};
    }

    private void startService() {
        // 一子反應器對應一條線程
        new Thread(bossReactor).start();
        new Thread(workReactors[0]).start();
        new Thread(workReactors[1]).start();
    }

    //反應器
    class Reactor implements Runnable {
        //每條線程負責一個選擇器的查詢
        final Selector selector;

        public Reactor(Selector selector) {
            this.selector = selector;
        }

        public void run() {
            try {
                while (!Thread.interrupted()) {
                    //單位為毫秒
                    selector.select(1000);
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    if (null == selectedKeys || selectedKeys.isEmpty()) {
                        continue;
                    }
                    Iterator<SelectionKey> keysIterator = selectedKeys.iterator();
                    while (keysIterator.hasNext()) {
                        //Reactor負責dispatch收到的事件
                        SelectionKey selectedKey = keysIterator.next();
                        dispatch(selectedKey);
                    }
                    selectedKeys.clear();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        void dispatch(SelectionKey sk) {
            Runnable handler = (Runnable) sk.attachment();
            //調用之前attach綁定到選擇鍵的handler處理器對象
            if (handler != null) {
                handler.run();
            }
        }
    }

    // Handler:新連接處理器
    class AcceptorHandler implements Runnable {
        public void run() {
            try {
                SocketChannel channel = serverSocketChannel.accept();
                logger.info("接收到一個新的連接");
                if (channel != null) {
                    int index = next.get();
                    logger.info("選擇器的編號：" + index);
                    Selector selector = workSelectors[index];
                    new MultiThreadEchoHandler(selector, channel);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (next.incrementAndGet() == workSelectors.length) {
                next.set(0);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        MultiThreadEchoServerReactor server = new MultiThreadEchoServerReactor();
        server.startService();
    }
}

