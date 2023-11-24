package ru.dyakun.proxy.message;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

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
        return buffer;
    }

    private static byte[] addressToBytes(AddressType type, Object addr) {
        return switch (type) {
            case IPV4, IPV6 -> {
                InetAddress address = (InetAddress) addr;
                yield address.getAddress();
            }
            case DOMAIN_NAME -> {
                String domainName = (String) addr;
                yield domainName.getBytes(StandardCharsets.US_ASCII);
            }
        };
    }

    public static ByteBuffer buildReplyMsg(ReplyCode code, AddressType type, Object bndAddr, int bndPort) {
        if(!UnsignedNumbers.isUnsignedShort(bndPort)) {
            throw new IllegalArgumentException("Bnd port must contain unsigned short value");
        }
        byte[] address = addressToBytes(type, bndAddr);
        ByteBuffer buffer = ByteBuffer.allocate(6 + address.length);
        buffer.put((byte) VERSION)
                .put((byte) code.getNumber())
                .put((byte) 0)
                .put((byte) type.getNumber())
                .put(address)
                .putShort((short) bndPort);
        return buffer;
    }

}
