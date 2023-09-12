package ru.nsu.dyakun;

import sun.misc.Signal;

import java.io.IOException;
import java.net.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MulticastMessenger {
    private final Map<InetAddress, LocalDateTime> members = new HashMap<>();
    private final InetSocketAddress groupAddr;
    private final int port;
    private MulticastSocket socket = null;
    private NetworkInterface netInterface = null;
    private boolean isRunning = false;

    MulticastMessenger(InetAddress groupAddr, int port) {
        Signal.handle(new Signal("INT"), signal -> stop());
        this.groupAddr = new InetSocketAddress(groupAddr, port);
        this.port = port;
    }

    public void run() {
        byte[] message = new byte[256];
        try {
            socket = new MulticastSocket(port);
            netInterface = getNetInterface();
            socket.joinGroup(groupAddr, netInterface);
            System.out.println("Joined in group " + groupAddr.getAddress());
            isRunning = true;
            send("Hello from ILya");
            while(isRunning) {
                DatagramPacket datagramPacket = new DatagramPacket(message, message.length);
                socket.receive(datagramPacket);
                members.put(datagramPacket.getAddress(), LocalDateTime.now());
                String msg = new String(message);
                System.out.printf("%s: %s%n", datagramPacket.getAddress().getHostAddress(), msg);
                send("echo: " + datagramPacket.getAddress());
                deleteInactiveMembers();
                printMembers();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void deleteInactiveMembers(){
        members.entrySet().removeIf(item -> ChronoUnit.SECONDS.between(item.getValue(), LocalDateTime.now()) > 15);
    }

    private void printMembers(){
        System.out.println("members:");
        for(var item : members.entrySet()){
            System.out.println("\t" + item.getKey().getHostAddress());
        }
    }

    private void send(String message) throws IOException {
        byte[] bytes = message.getBytes();
        var packet = new DatagramPacket(bytes, bytes.length, groupAddr.getAddress(), port);
        socket.send(packet);
    }

    private void stop() {
        if(socket == null) return;
        try {
            socket.leaveGroup(groupAddr, netInterface);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            isRunning = false;
            System.out.println("Leave from group");
        }
    }

    private static NetworkInterface getNetInterface() throws SocketException {
        var interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
        System.out.println("Network interfaces count: " + interfaces.size());
        NetworkInterface networkInterface = null;
        for (NetworkInterface netInterface : interfaces) {
            if (netInterface.isUp() && !netInterface.isLoopback()) {
                System.out.println("Interface name: " + netInterface.getDisplayName());
                System.out.println("\tInterface addresses: ");
                for (InterfaceAddress addr : netInterface.getInterfaceAddresses()) {
                    System.out.println("\t\t" + addr.getAddress().toString());
                }
                networkInterface = netInterface;
            }
        }
        if(networkInterface != null) return networkInterface;
        throw new SocketException("No such network interface");
    }
}
