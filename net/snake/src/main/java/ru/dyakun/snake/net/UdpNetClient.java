package ru.dyakun.snake.net;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.model.util.MessageType;
import ru.dyakun.snake.protocol.GameMessage;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class UdpNetClient implements NetClient {
    private static final Logger logger = LoggerFactory.getLogger(UdpNetClient.class);
    private final DatagramSocket socket;
    private final List<GameMessageListener> listeners = new ArrayList<>();
    private final BlockingQueue<GameMessage> pending = new ArrayBlockingQueue<>(30);
    private final MessageSender sender;
    private boolean isRunning = false;

    public UdpNetClient() {
        try {
            socket = new DatagramSocket();
            sender = new MessageSender(pending, socket, this);
            new Thread(sender).start();
            // TODO refactor
        } catch (SocketException e) {
            logger.info("Udp socket create failed");
            throw new IllegalStateException(e);
        }
    }

    private void notifyListeners(GameMessage message, InetSocketAddress sender) {
        for(var listener : listeners) {
            listener.handle(message, sender);
        }
    }

    @Override
    public void send(MessageType type, GameMessage message, InetSocketAddress receiver) {
        // TODO impl
    }

    @Override
    public void addMessageListener(GameMessageListener listener) {
        listeners.add(listener);
    }

    @Override
    public void run() {
        byte[] buf = new byte[4096];
        isRunning = true;
        try {
            while (isRunning) {
                DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
                socket.receive(datagramPacket);
                logger.debug("Receive from [{}]", datagramPacket.getAddress().getHostAddress());
                try {
                    notifyListeners(GameMessage.parseFrom(buf), (InetSocketAddress) datagramPacket.getSocketAddress());
                } catch (InvalidProtocolBufferException e) {
                    logger.info("Receive broken protobuf");
                }
            }
        } catch (SocketException e) {
            logger.debug("Socket", e);
        } catch (IOException e) {
            logger.info("Udp receive failed", e);
        }
        logger.info("Udp receiver successfully stopped");
    }

    @Override
    public void stop() {
        if(socket.isClosed()) return;
        isRunning = false;
        socket.close();
        sender.stop();
        logger.debug("Close udp socket");
    }

    private static class MessageSender implements Runnable, Stoppable{
        private final BlockingQueue<GameMessage> queue;
        private final DatagramSocket socket;
        private boolean isRunning = false;

        public MessageSender(BlockingQueue<GameMessage> queue, DatagramSocket socket, UdpNetClient client) {
            this.queue = queue;
            this.socket = socket;
            // TODO ack receive
        }

        @Override
        public void run() {
            isRunning = true;
            try {
                while (isRunning) {
                    GameMessage message = queue.take();
                    byte[] buf = message.toByteArray();
                    DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
                    socket.send(datagramPacket);
                }
            } catch (IOException e) {
                logger.info("Udp send failed", e);
            } catch (InterruptedException e) {
                logger.info("Udp send interrupted", e);
            }
        }

        @Override
        public void stop() {
            isRunning = false;
        }
    }
}
