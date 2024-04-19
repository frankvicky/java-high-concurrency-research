package frankvicky.cc.protocol;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ProtobufDemo {
    private static final Logger logger = LoggerFactory.getLogger(ProtobufDemo.class);
    public static MsgProtos.Msg buildMsg() {
        MsgProtos.Msg.Builder personBuilder = MsgProtos.Msg.newBuilder();
        personBuilder.setId(1000);
        personBuilder.setContent("瘋狂創客圈：高性能學習社群");
        return personBuilder.build();
    }

    @Test
    public void serAndDesr1() throws IOException {
        MsgProtos.Msg message = buildMsg();
        byte[] data = message.toByteArray();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // serialization
        outputStream.write(data);

        // deserialization from byte array
        MsgProtos.Msg inMessage = MsgProtos.Msg.parseFrom(data);
        logger.info("id: {}", inMessage.getId());
        logger.info("content: {}", inMessage.getContent());
    }

    @Test
    public void serAndDesr2() throws IOException {
        MsgProtos.Msg message = buildMsg();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        message.writeTo(outputStream);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        // deserialization from byteArrayInputStream
        MsgProtos.Msg inMessage = MsgProtos.Msg.parseFrom(inputStream);
        logger.info("id: {}", inMessage.getId());
        logger.info("content: {}", inMessage.getContent());
    }

    @Test
    public void serAndDesr3() throws IOException {
        MsgProtos.Msg message = buildMsg();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        message.writeDelimitedTo(outputStream);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        // parseDelimitedFrom 會讀取 varint32，根據實際長度讀取 byte array，以此解決半包、黏包問題
        MsgProtos.Msg inMessage = MsgProtos.Msg.parseDelimitedFrom(inputStream);
        logger.info("id: {}", inMessage.getId());
        logger.info("content: {}", inMessage.getContent());
    }
}
