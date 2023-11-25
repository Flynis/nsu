package ru.dyakun.proxy.message;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class RequestMsg {
    private final byte version;
    private final RequestCommand command;
    private final AddressType addressType;
    private final Object dstAddress;
    private final int dstPort;

    private RequestMsg(byte version, RequestCommand command, AddressType addressType, Object dstAddress, int dstPort) {
        this.version = version;
        this.command = command;
        this.addressType = addressType;
        this.dstAddress = dstAddress;
        this.dstPort = dstPort;
    }

    public byte getVersion() {
        return version;
    }

    public RequestCommand getCommand() {
        return command;
    }

    public AddressType getAddressType() {
        return addressType;
    }

    public int getDstPort() {
        return dstPort;
    }

    public InetAddress getDstAddress() {
        return (InetAddress) dstAddress;
    }

    public String getDstDomainName() {
        return (String) dstAddress;
    }

    public static RequestMsg parseFrom(ByteBuffer buffer) throws MessageParseException {
        try {
            byte version = buffer.get();
            byte cmd = buffer.get();
            RequestCommand command = RequestCommand.fromNumber(UnsignedNumbers.getUnsignedByte(cmd));
            byte ignoredReserved = buffer.get();
            byte addrType = buffer.get();
            AddressType type = AddressType.fromNumber(UnsignedNumbers.getUnsignedByte(addrType));
            Object dstAddress;
            switch (type) {
                case IPV4 -> {
                    byte[] address = new byte[4];
                    buffer.get(address);
                    dstAddress = Inet4Address.getByAddress(address);
                }
                case DOMAIN_NAME -> {
                    byte[] bytes = SocksMessages.parseByteArray(buffer);
                    dstAddress = new String(bytes, StandardCharsets.US_ASCII);
                }
                case IPV6 -> {
                    byte[] address = new byte[16];
                    buffer.get(address);
                    dstAddress = Inet6Address.getByAddress(address);
                }
                default -> throw new MessageParseException("Unknown address type");
            }
            short dstPort = buffer.getShort();
            int port = UnsignedNumbers.getUnsignedShort(dstPort);
            return new RequestMsg(version, command, type, dstAddress, port);
        } catch (BufferUnderflowException | UnknownHostException e) {
            throw new MessageParseException("Request msg parse failed", e);
        }
    }

    @Override
    public String toString() {
        int ver = UnsignedNumbers.getUnsignedByte(version);
        String addr = switch (addressType) {
            case IPV4, IPV6 -> {
                InetAddress inetAddress = (InetAddress) dstAddress;
                yield inetAddress.getHostAddress();
            }
            case DOMAIN_NAME -> (String) dstAddress;
        };
        return String.format("Request[ver%d cmd=%d a.typ=%d dst.Addr=%s dst.port=%d]",
                ver,
                command.getNumber(),
                addressType.getNumber(),
                addr,
                dstPort);
    }

}
