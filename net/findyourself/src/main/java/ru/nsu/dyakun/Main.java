package ru.nsu.dyakun;

import java.net.InetAddress;

public class Main {
    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Expect one option: link local multicast address");
            return;
        }
        try {
            InetAddress groupAddr = LinkLocalMulticastAddrParser.parse(args[0]);
            MulticastMessenger messenger = new MulticastMessenger(groupAddr, 8888);
            messenger.run();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}