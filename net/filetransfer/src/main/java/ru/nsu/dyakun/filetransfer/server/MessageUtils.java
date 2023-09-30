package ru.nsu.dyakun.filetransfer.server;

import ru.nsu.dyakun.filetransfer.protocol.Constants;
import ru.nsu.dyakun.filetransfer.protocol.Message;
import ru.nsu.dyakun.filetransfer.protocol.MessageType;

import java.nio.ByteBuffer;

import static ru.nsu.dyakun.filetransfer.protocol.Constants.*;

public class MessageUtils {
    private MessageUtils() {
        throw new AssertionError();
    }

    public static int writeStringInMessage(MessageType type, String str, byte[] buffer) {
        return Message.writeIn(type, str.getBytes(MESSAGE_CHARSET), buffer);
    }

    public static FileAttrs readFileAttrsFrom(Message message) {
        if(message.getType() != MessageType.UPLOAD_REQUEST) {
            throw new IllegalArgumentException("Expect upload request: " + message.getType());
        }
        if(message.getLength() <= FILE_LEN_SIZE) {
            throw new IllegalStateException("Payload is too short");
        }
        byte[] payload = message.getPayload();
        ByteBuffer byteBuffer = ByteBuffer.wrap(payload);
        long length = byteBuffer.getLong();
        String name = new String(payload, FILE_LEN_SIZE, payload.length);
        return new FileAttrs(name, length);
    }
}
