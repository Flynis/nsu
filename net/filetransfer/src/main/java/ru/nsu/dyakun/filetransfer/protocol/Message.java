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
        return new Message(type, length, payload);
    }

    public static int writeIn(MessageType type, byte[] payload, byte[] buffer) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        if(buffer.length < MESSAGE_HEADER_SIZE) {
            throw new IllegalStateException("Buffer length must be >= MESSAGE_HEADER_SIZE");
        }
        byteBuffer.put((byte) type.getCode());
        if(payload == null) {
            byteBuffer.putInt(0);
            return MESSAGE_HEADER_SIZE;
        }
        int messageLength = MESSAGE_HEADER_SIZE + payload.length;
        if(buffer.length < messageLength) {
            throw new IllegalStateException("Buffer length must be >= message length");
        }
        byteBuffer.put(payload);
        return messageLength;
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
