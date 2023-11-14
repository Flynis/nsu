package ru.dyakun.snake.net;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.protocol.GameMessage;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

public class MulticastMessageReceiver implements MessageReceiver {
    private static final Logger logger = LoggerFactory.getLogger(MulticastMessageReceiver.class);
    private final List<GameMessageListener> listeners = new ArrayList<>();
    private final InetSocketAddress group;
    private MulticastSocket socket = null;
    private boolean isRunning = false;
    private final List<NetworkInterface> interfaces = new ArrayList<>();

    public MulticastMessageReceiver(InetSocketAddress group) {
        this.group = Objects.requireNonNull(group);
    }

    private void notifyListeners(GameMessage message, InetSocketAddress sender) {
        try {
            for(var listener : listeners) {
                listener.onMessage(message, sender);
            }
        } catch (Exception e) {
            logger.error("Handle message failed", e);
        }
    }

    @Override
    public void addMessageListener(GameMessageListener listener) {
        listeners.add(listener);
    }

    private void join() throws IOException {
        socket = new MulticastSocket(group.getPort());
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            if (networkInterface.isLoopback() || !networkInterface.isUp()) continue;
            socket.joinGroup(group, networkInterface);
            interfaces.add(networkInterface);
            logger.debug("Joined in group [{}] from {}", group.getAddress().getHostAddress(), networkInterface.getName());
            networkInterface.inetAddresses().forEach(address -> logger.debug("Address: {}", address.getHostAddress()));
        }
    }

    private void leave() {
        for(var networkInterface : interfaces) {
            logger.debug("Leave from group on {}", networkInterface.getName());
            try {
                socket.leaveGroup(group, networkInterface);
            } catch (IOException e) {
                logger.error("Leave from group failed");
            }
        }
    }

    @Override
    public void run() {
        byte[] buf = new byte[4096];
        try {
            join();
            isRunning = true;
            while(isRunning) {
                DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
                socket.receive(datagramPacket);
                logger.debug("Receive from [{}] {}b", datagramPacket.getAddress().getHostAddress(), datagramPacket.getLength());
                try {
                    ByteBuffer buffer = ByteBuffer.wrap(buf, 0, datagramPacket.getLength());
                    var message = GameMessage.parseFrom(buffer);
                    notifyListeners(message, (InetSocketAddress) datagramPacket.getSocketAddress());
                } catch (InvalidProtocolBufferException e) {
                    logger.info("Receive broken protobuf", e);
                }
            }
        } catch (SocketException e) {
            logger.debug("Socket", e);
        } catch (IOException e) {
            logger.error("Multicast receive failed", e);
        } catch (Exception e) {
            logger.error("Fatal error in multicast receiver", e);
        }
        logger.info("Multicast receiver successfully stopped");
    }

    @Override
    public void stop() {
        if(socket == null || socket.isClosed()) return;
        isRunning = false;
        leave();
        socket.close();
        logger.debug("Close multicast socket");
    }
}
