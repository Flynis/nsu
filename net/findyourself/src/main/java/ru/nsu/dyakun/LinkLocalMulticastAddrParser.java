package ru.nsu.dyakun;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class LinkLocalMulticastAddrParser {
    public static InetAddress parse(String str) {
        try {
            InetAddress addr = InetAddress.getByName(str);
            if(!addr.isMulticastAddress()) {
                throw new IllegalArgumentException("Address must be multicast");
            }
            if(addr instanceof Inet4Address addr4 && isLinkLocal4Address(addr4) ||
                    addr instanceof Inet6Address addr6 && isLinkLocal6Address(addr6)) {
                return addr;
            } else {
                throw new IllegalArgumentException("Address must be link local");
            }
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Str must contain ip address");
        }
    }

    private static boolean isLinkLocal4Address(Inet4Address addr) {
        byte[] bytes = addr.getAddress();
        return bytes[0] == Integer.valueOf(224).byteValue()
                && bytes[1] == 0
                && bytes[2] == 0
                && bytes[3] != 0
                && bytes[3] != Integer.valueOf(255).byteValue();
    }

    private static boolean isLinkLocal6Address(Inet6Address addr) {
        byte[] bytes = addr.getAddress();
        return bytes[0] == Integer.valueOf(0xFF).byteValue() && bytes[1] == 2;
    }
}
