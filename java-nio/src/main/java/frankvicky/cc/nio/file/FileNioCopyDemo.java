package frankvicky.cc.nio.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileNioCopyDemo {
    private static final Logger logger = LoggerFactory.getLogger(FileNioCopyDemo.class);

    public static void main(String[] args) {
        String originFilePath = "java-nio/src/main/resources/FileChannelExample/origin.txt";
        String copyFilePath = "java-nio/src/main/resources/FileChannelExample/copy.txt";

        File origin = new File(originFilePath);
        File copy = new File(copyFilePath);

        try {
            if (!copy.exists()) {
                copy.createNewFile();
            }

            long startTime = System.currentTimeMillis();
            FileInputStream fileInputStream = null;
            FileOutputStream fileOutputStream = null;
            FileChannel inputChannel = null, outputChannel = null;

            try {
                fileInputStream = new FileInputStream(origin);
                fileOutputStream = new FileOutputStream(copy);
                inputChannel = fileInputStream.getChannel();
                outputChannel = fileOutputStream.getChannel();

                ByteBuffer buffer = ByteBuffer.allocate(1024);
                int length = -1;

                // 從 inputChannel 讀取資料然後寫入 buffer
                // 此處的 buffer 是屬於 io 模型中的 user buffer
                // inputChannel 是對於文件描述符的抽象
                while ((length = inputChannel.read(buffer)) != -1) {
                    // buffer 寫滿了，於是翻轉為讀模式
                    buffer.flip();
                    int outLength = 0;
                    // outputChannel 從 buffer 讀取資料並寫入自身
                    while ((outLength = outputChannel.write(buffer)) != 0) {
                        System.out.println("Written bytes: " + outLength);
                    }
                    // buffer 完成一次讀寫，於是清空並轉換為寫模式，為下一次讀寫作準備
                    buffer.clear();
                }
                outputChannel.force(true);
            } finally {
                outputChannel.close();
                fileOutputStream.close();
                inputChannel.close();
                fileInputStream.close();
            }

            long endTime = System.currentTimeMillis();
            logger.info("Cost time(ms): " + (endTime - startTime));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
