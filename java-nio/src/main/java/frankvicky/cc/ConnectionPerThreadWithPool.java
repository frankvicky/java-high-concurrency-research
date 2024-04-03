package frankvicky.cc;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectionPerThreadWithPool implements Runnable {

    // 一個 Connection per Thread 範例

    @Override
    public void run() {
        // 生產環境不可以這樣寫
        ExecutorService executor = Executors.newFixedThreadPool(100);

        try {
            // 伺服器監聽 socket
            ServerSocket serverSocket = new ServerSocket(8181);

            // 無窮迴圈，用以等待新連線
            while (!Thread.interrupted()) {
                Socket socket = serverSocket.accept();
                // 每當有一個新連接就建立一個新的 Handler 專門處理
                Handler handler = new Handler(socket);
                executor.execute(handler);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    static class Handler implements Runnable {

        final Socket socket;

        Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            boolean ioCompleted = false;

            // 無窮迴圈，處理讀寫事件
            while (!ioCompleted) {
                try {
                    byte[] input = new byte[5000];
                    socket.getInputStream().read(input);
                    ioCompleted = true;
                    socket.close();
                    byte[] output = null;
                    socket.getOutputStream().write(output);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
