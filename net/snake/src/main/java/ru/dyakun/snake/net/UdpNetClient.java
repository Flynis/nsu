package ru.dyakun.snake.net;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.game.util.MessageType;
import ru.dyakun.snake.protocol.GameMessage;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class UdpNetClient implements NetClient {
    private static final Logger logger = LoggerFactory.getLogger(UdpNetClient.class);
    private final DatagramSocket socket;
    private final List<GameMessageListener> listeners = new ArrayList<>();
    private final BlockingQueue<SendData> pending = new ArrayBlockingQueue<>(30);
    private final MessageSender messageSender;
    private boolean isRunning = false;

    public UdpNetClient() {
        try {
            socket = new DatagramSocket();
            messageSender = new MessageSender(pending, socket);
            var thread = new Thread(messageSender);
            thread.setName("Sender");
            thread.start();
        } catch (SocketException e) {
            logger.error("Udp socket create failed");
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void changeReceiver(InetSocketAddress current, InetSocketAddress old) {
        messageSender.changeReceiver(current, old);
    }

    @Override
    public void removeReceiver(InetSocketAddress address) {
        messageSender.removeReceiver(address);
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
    public void send(MessageType type, GameMessage message, InetSocketAddress receiver) {
        try {
            pending.put(new SendData(type, message, receiver));
        } catch (InterruptedException e) {
            logger.info("Interrupted pending put", e);
        }
    }

    @Override
    public void setTimeout(int timeout) {
        if(timeout < 10) {
            throw new IllegalArgumentException("Too small timeout");
        }
        messageSender.setTimeout(timeout);
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
                logger.debug("Receive from [{}] {}b", datagramPacket.getAddress().getHostAddress(), datagramPacket.getLength());
                try {
                    ByteBuffer buffer = ByteBuffer.wrap(buf, 0, datagramPacket.getLength());
                    var message = GameMessage.parseFrom(buffer);
                    var sender = (InetSocketAddress) datagramPacket.getSocketAddress();
                    if(message.hasAck()) {
                        messageSender.handleAck(message, sender);
                    }
                    notifyListeners(message, sender);
                } catch (InvalidProtocolBufferException e) {
                    logger.info("Receive broken protobuf");
                }
            }
        } catch (SocketException e) {
            logger.debug("Socket", e);
        } catch (IOException e) {
            logger.error("Udp receive failed", e);
        } catch (Exception e) {
            logger.error("Fatal error in udp receiver", e);
        }
        logger.info("Udp receiver successfully stopped");
    }

    @Override
    public void stop() {
        if(socket.isClosed()) return;
        isRunning = false;
        messageSender.stop();
        socket.close();
        logger.debug("Close udp socket");
    }
}
