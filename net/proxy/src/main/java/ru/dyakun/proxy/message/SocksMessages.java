package ru.dyakun.proxy.message;

import java.net.InetAddress;
import java.nio.ByteBuffer;

public class SocksMessages {
    public static final int NO_AUTHENTICATION_REQUIRED = 0;
    public static final int VERSION = 5;

    public static byte[] parseByteArray(ByteBuffer buffer) {
        byte len = buffer.get();
        int length = UnsignedNumbers.getUnsignedByte(len);
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return bytes;
    }

    public static ByteBuffer buildHandshakeReplyMsg(int method) {
        if(!UnsignedNumbers.isUnsignedByte(method)) {
            throw new IllegalArgumentException("Method must contain unsigned byte value");
        }
        ByteBuffer buffer =  ByteBuffer.allocate(2);
        buffer.put((byte) VERSION)
                .put((byte) method);
        buffer.flip();
        return buffer;
    }

    public static ByteBuffer buildReplyMsg(ReplyCode code, InetAddress address, int bndPort) {
        if(!UnsignedNumbers.isUnsignedShort(bndPort)) {
            throw new IllegalArgumentException("Bnd port must contain unsigned short value");
        }
        byte[] addressBytes = address.getAddress();
        ByteBuffer buffer = ByteBuffer.allocate(6 + addressBytes.length);
        buffer.put((byte) VERSION)
                .put((byte) code.getNumber())
                .put((byte) 0)
                .put((byte) AddressType.IPV4.getNumber())
                .put(addressBytes)
                .putShort((short) bndPort);
        buffer.flip();
        return buffer;
    }

}
