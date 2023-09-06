package ru.nsu.dyakun;

public class Main {
    public static void main(String[] args) {
        NetworkInterface ni = NetworkInterface.getByName("hme0");

        DatagramChannel dc = DatagramChannel.open(StandardProtocolFamily.INET)
                .setOption(StandardSocketOptions.SO_REUSEADDR, true)
                .bind(new InetSocketAddress(5000))
                .setOption(StandardSocketOptions.IP_MULTICAST_IF, ni);

        InetAddress group = InetAddress.getByName("225.4.5.6");

        MembershipKey key = dc.join(group, ni);
    }
}