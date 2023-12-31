package ru.dyakun.snake.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.protocol.GameMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

import static ru.dyakun.snake.game.util.MessageType.*;

public class MessageSender implements Runnable, Stoppable {
    private static final Logger logger = LoggerFactory.getLogger(MessageSender.class);
    private static final int MAX_SEND_COUNT = 10;
    private final BlockingQueue<SendData> queue;
    private final Queue<SendData> ackWaiters = new ConcurrentLinkedDeque<>();
    private final List<GameMessageListener> listeners;
    private final DatagramSocket socket;
    private boolean isRunning = false;
    private long timeout = 100;

    MessageSender(BlockingQueue<SendData> queue, List<GameMessageListener> listeners, DatagramSocket socket) {
        this.queue = queue;
        this.listeners = listeners;
        this.socket = socket;
    }

    private void notifyListenersOnError(GameMessage message) {
        try {
            for(var listener : listeners) {
                listener.onSendError(message);
            }
        } catch (Exception e) {
            logger.error("On send error handle failed", e);
        }
    }

    void changeReceiver(InetSocketAddress current, InetSocketAddress old) {
        logger.debug("Change receive from {} to {}", old.getAddress().getHostAddress(), current.getAddress().getHostAddress());
        var resendList = new ArrayList<SendData>();
        for(var data : ackWaiters) {
            if(data.receiver().equals(old)) {
                data.setReceiver(current);
                resendList.add(data);
            }
        }
        ackWaiters.removeAll(resendList);
        queue.addAll(resendList);
    }

    void removeReceiver(InetSocketAddress address) {
        logger.debug("Remove receiver {}", address.getAddress().getHostAddress());
        queue.removeIf(data -> address.equals(data.receiver()));
        ackWaiters.removeIf(data -> address.equals(data.receiver()));
    }

    void setTimeout(int timeout) {
        logger.debug("Change timeout to {}", timeout);
        this.timeout = timeout;
    }

    @Override
    public void run() {
        isRunning = true;
        try {
            while (isRunning) {
                SendData sendData = queue.poll(timeout, TimeUnit.MILLISECONDS);
                if(sendData == null) {
                    resend();
                    continue;
                }
                var type = sendData.type();
                if(type != ACK && type != ANNOUNCEMENT && type != DISCOVER) {
                    sendData.setSendTime(LocalDateTime.now());
                    sendData.incSendCount();
                    ackWaiters.add(sendData);
                }
                byte[] buf = sendData.message().toByteArray();
                logger.debug("Send {} {}b {}", sendData, buf.length, LocalDateTime.now());
                DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, sendData.receiver());
                socket.send(datagramPacket);
            }
        } catch (IOException e) {
            logger.error("Udp send failed", e);
        } catch (InterruptedException e) {
            logger.error("Udp send interrupted", e);
        }
    }

    private void resend() {
        var resendList = new ArrayList<SendData>();
        for(var data : ackWaiters) {
            if(data.sendCount() == MAX_SEND_COUNT) {
                notifyListenersOnError(data.message());
                return;
            }
            if(ChronoUnit.MILLIS.between(data.sendTime(), LocalDateTime.now()) > timeout) {
                resendList.add(data);
            }
        }
        ackWaiters.removeAll(resendList);
        queue.addAll(resendList);
    }

    void handleAck(GameMessage message, InetSocketAddress sender) {
        ackWaiters.removeIf(data -> data.message().getMsgSeq() == message.getMsgSeq() && data.receiver().equals(sender));
    }

    void handleError() {
        ackWaiters.removeIf(data -> data.type() == JOIN);
    }

    @Override
    public void stop() {
        while (!queue.isEmpty()) {
            try {
                ackWaiters.clear();
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.error("Interrupted last messages send");
            }
        }
        isRunning = false;
    }
}
