package ru.nsu.dyakun.filetransfer.client;

import ru.nsu.dyakun.filetransfer.protocol.Message;
import ru.nsu.dyakun.filetransfer.protocol.MessageType;

import java.nio.ByteBuffer;

import static ru.nsu.dyakun.filetransfer.protocol.Constants.*;

public class MessageUtils {
    private MessageUtils() {
        throw new AssertionError();
    }

    public static int writeUploadRequest(long fileLength, String fileName, byte[] buffer) {
        System.out.printf("Send file %s: %d%n", fileName, fileLength);
        if(fileLength <= 0) {
            throw new IllegalArgumentException("File length must be > 0");
        }
        if(fileName.isBlank()) {
            throw new IllegalArgumentException("File name is blank");
        }
        byte[] bytes = fileName.getBytes(MESSAGE_CHARSET);
        int length = FILE_LEN_SIZE + bytes.length;
        byte[] payload = new byte[length];
        var byteBuffer = ByteBuffer.wrap(payload);
        byteBuffer.putLong(fileLength);
        byteBuffer.put(bytes);
        return Message.writeIn(MessageType.UPLOAD_REQUEST, payload, buffer);
    }

    public static String readStringFrom(Message message) {
        if(message.getLength() == 0) return "";
        return new String(message.getPayload(), 0, message.getLength());
    }
}
