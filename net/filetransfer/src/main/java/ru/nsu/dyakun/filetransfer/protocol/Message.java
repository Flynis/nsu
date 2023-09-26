package ru.nsu.dyakun.filetransfer.protocol;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import static ru.nsu.dyakun.filetransfer.protocol.Constants.*;

public class Message {
    private final MessageType type;
    private final long length;
    private final byte[] payload;
    private final byte[] bytes;

    public static Message from(byte[] bytes) {
        return new Message(bytes);
    }

    private Message(byte[] bytes) {
        if(bytes.length < MESSAGE_MIN_LENGTH) {
            throw new IllegalArgumentException("Bytes doesn't contain message");
        }
        this.bytes = bytes;
        this.type = MessageType.valueOf(bytes[0]);
        this.length = ByteBuffer.wrap(bytes, MESSAGE_TYPE_SIZE, MESSAGE_LEN_SIZE).getLong();
        this.payload = ByteBuffer.wrap(bytes, MESSAGE_MIN_LENGTH, bytes.length).array();
    }

    public static Message of(MessageType type, long length, byte[] payload) {
        return new Message(type, length, payload);
    }

    private Message(MessageType type, long length, byte[] payload) {
        if(length < 0) {
            throw new IllegalArgumentException("Length must be > 0");
        }
        ByteBuffer buffer = ByteBuffer.allocate(MESSAGE_MIN_LENGTH + payload.length);
        buffer.put((byte) type.getCode());
        buffer.putLong(length);
        buffer.put(payload);
        this.type = type;
        this.length = length;
        this.payload = payload;
        this.bytes = buffer.array();
    }

    public MessageType getType() {
        return type;
    }

    public long getLength() {
        return length;
    }

    public byte[] getPayload() {
        return payload;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
