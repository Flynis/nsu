package ru.dyakun.snake.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.protocol.GameMessage;
import ru.dyakun.snake.protocol.GameMessageOrBuilder;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MulticastMessageReceiver implements MessageReceiver {
    private static final Logger logger = LoggerFactory.getLogger(MulticastMessageReceiver.class);
    private final List<GameMessageListener> listeners = new ArrayList<>();
    private final InetSocketAddress groupAddr;
    private final int port;
    private MulticastSocket socket = null;
    private NetworkInterface netInterface = null;
    private boolean isRunning = false;

    public MulticastMessageReceiver(InetAddress groupAddr, int port) {
        this.groupAddr = new InetSocketAddress(groupAddr, port);
        this.port = port;
    }

    private void notifyListeners(GameMessageOrBuilder message, InetAddress receiver) {
        for(var listener : listeners) {
            listener.handle(message, receiver);
        }
    }

    @Override
    public void addMessageListener(GameMessageListener listener) {
        listeners.add(listener);
    }

    @Override
    public void run() {
        byte[] buf = new byte[4096];
        try {
            socket = new MulticastSocket(port);
            netInterface = getNetInterface();
            socket.joinGroup(groupAddr, netInterface);
            logger.debug("Joined in group [{}]", groupAddr.getAddress());
            isRunning = true;
            while(isRunning) {
                DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
                socket.receive(datagramPacket);
                logger.debug("Receive from [{}]", datagramPacket.getAddress().getHostAddress());
                notifyListeners(GameMessage.parseFrom(buf), datagramPacket.getAddress());
            }
        } catch (IOException e) {
            logger.info("Multicast receive failed", e);
        }
    }

    @Override
    public void stop() {
        if(socket == null || socket.isClosed()) return;
        isRunning = false;
        try {
            socket.leaveGroup(groupAddr, netInterface);
            logger.debug("Leave from group");
            socket.close();
            logger.debug("Close multicast socket");
        } catch (IOException e) {
            logger.info("Multicast receiver stop failed", e);
            if(!socket.isClosed()) {
                stop();
            }
        }
    }

    private static NetworkInterface getNetInterface() throws SocketException {
        var interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
        logger.debug("Network interfaces count: {}", interfaces.size());
        NetworkInterface networkInterface = null;
        for (NetworkInterface netInterface : interfaces) {
            if (netInterface.isUp() && !netInterface.isLoopback()) {
                logger.debug("Interface name: {}", netInterface.getDisplayName());
                logger.debug("\tInterface addresses: ");
                for (InterfaceAddress addr : netInterface.getInterfaceAddresses()) {
                    logger.debug("\t\t{}", addr.getAddress().toString());
                }
                networkInterface = netInterface;
            }
        }
        if(networkInterface != null) return networkInterface;
        throw new SocketException("No such network interface");
    }
}