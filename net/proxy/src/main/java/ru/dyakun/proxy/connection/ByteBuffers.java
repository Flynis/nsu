package ru.dyakun.proxy.connection;

import java.nio.ByteBuffer;

public class ByteBuffers {

    private ByteBuffers() {
        throw new AssertionError();
    }

    public static ByteBuffer copyFrom(ByteBuffer in) {
        ByteBuffer out = ByteBuffer.allocate(in.limit());
        out.put(in);
        out.flip();
        return out;
    }

}
