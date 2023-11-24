package ru.dyakun.proxy.message;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class HandshakeMsg {
    private final byte version;
    private final byte[] methods;

    private HandshakeMsg(byte version, byte[] methods) {
        this.version = version;
        this.methods = methods;
    }

    public byte getVersion() {
        return version;
    }

    public byte[] getMethods() {
        return methods;
    }

    public static HandshakeMsg parseFrom(ByteBuffer buffer) throws MessageParseException {
        try {
            byte version = buffer.get();
            byte[] methods = SocksMessages.parseByteArray(buffer);
            return new HandshakeMsg(version, methods);
        } catch (BufferUnderflowException e) {
            throw new MessageParseException("Handshake msg parse failed", e);
        }
    }

}