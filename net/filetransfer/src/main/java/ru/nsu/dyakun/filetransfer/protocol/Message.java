package ru.nsu.dyakun.filetransfer.protocol;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import static ru.nsu.dyakun.filetransfer.protocol.Constants.*;

public class Message {
    private final MessageType type;
    private final int length;
    private final byte[] payload;

    public static Message readFrom(DataInputStream in, byte[] payload) throws IOException {
        MessageType type = MessageType.valueOf(in.readByte());
        int length = in.readInt();
        in.readFully(payload, 0 , length);
        //System.out.printf("Recv [%s] %d%n", type, length);
        return new Message(type, length, payload);
    }

    public static int writeIn(MessageType type, byte[] payload, int length, byte[] buffer) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        if(buffer.length < MESSAGE_HEADER_SIZE) {
            throw new IllegalStateException("Buffer length must be >= MESSAGE_HEADER_SIZE");
        }
        byteBuffer.put((byte) type.getCode());
        if(payload == null) {
            byteBuffer.putInt(0);
            //System.out.printf("Send [%s] %d%n", type, 0);
            return MESSAGE_HEADER_SIZE;
        }
        int messageLength = MESSAGE_HEADER_SIZE + length;
        if(buffer.length < messageLength) {
            throw new IllegalStateException("Buffer length must be >= message length");
        }
        byteBuffer.putInt(length);
        byteBuffer.put(payload, 0, length);
        //System.out.printf("Send [%s] %d: %s", type, length, Arrays.toString(buffer));
        //System.out.printf("Send [%s] %d%n", type, length);
        return messageLength;
    }

    public static int writeIn(MessageType type, byte[] payload, byte[] buffer) {
        if(payload == null) {
            return writeIn(type, null, 0, buffer);
        }
        return writeIn(type, payload, payload.length, buffer);
    }

    private Message(MessageType type, int length, byte[] payload) {
        this.type = type;
        this.length = length;
        this.payload = payload;
    }

    public MessageType getType() {
        return type;
    }

    public int getLength() {
        return length;
    }

    public byte[] getPayload() {
        return payload;
    }
}
