package ru.dyakun.snake.net;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.protocol.GameMessage;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MulticastMessageReceiver implements MessageReceiver {
    private static final Logger logger = LoggerFactory.getLogger(MulticastMessageReceiver.class);
    private final List<GameMessageListener> listeners = new ArrayList<>();
    private final InetSocketAddress group;
    private MulticastSocket socket = null;
    private boolean isRunning = false;

    public MulticastMessageReceiver(InetSocketAddress group) {
        this.group = Objects.requireNonNull(group);
    }

    private void notifyListeners(GameMessage message, InetSocketAddress sender) {
        try {
            for(var listener : listeners) {
                listener.handle(message, sender);
            }
        } catch (Exception e) {
            logger.error("Handle message failed", e);
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
            socket = new MulticastSocket(group.getPort());
            socket.joinGroup(group.getAddress());
            logger.debug("Joined in group [{}]", group.getAddress().getHostAddress());
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
        logger.info("Try to stop Multicast receiver");
        isRunning = false;
        try {
            socket.leaveGroup(group.getAddress());
            logger.debug("Leave from group");
            socket.close();
            logger.debug("Close multicast socket");
        } catch (IOException e) {
            logger.error("Multicast receiver stop failed", e);
            if(!socket.isClosed()) {
                stop();
            }
        }
    }
}
