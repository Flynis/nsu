package ru.dyakun.proxy.message;

import java.nio.ByteBuffer;

public class ConnectMsg {
    private byte version;
    private byte[] methods;

    private ConnectMsg(byte version, byte[] methods) {
        this.version = version;
        this.methods = methods;
    }

    public byte getVersion() {
        return version;
    }

    public byte[] getMethods() {
        return methods;
    }

    public static ConnectMsg parseFrom(ByteBuffer buffer) {

    }

}
