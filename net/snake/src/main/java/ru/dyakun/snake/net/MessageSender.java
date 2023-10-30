package ru.dyakun.snake.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.protocol.GameMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static ru.dyakun.snake.model.util.MessageType.*;

public class MessageSender implements Runnable, Stoppable {
    private static final Logger logger = LoggerFactory.getLogger(MessageSender.class);
    private final BlockingQueue<SendData> queue;
    private final BlockingQueue<SendData> ackWaiters = new ArrayBlockingQueue<>(30);
    private final DatagramSocket socket;
    private final NetClient client;
    private boolean isRunning = false;
    private long timeout = 10000;

    MessageSender(BlockingQueue<SendData> queue, DatagramSocket socket, UdpNetClient client) {
        this.queue = queue;
        this.socket = socket;
        this.client = client;
    }

    void changeReceiver(InetSocketAddress current, InetSocketAddress old) {

    }

    void setTimeout(int timeout) {
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
                    ackWaiters.put(sendData);
                }
                byte[] buf = sendData.message().toByteArray();
                DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
                socket.send(datagramPacket);
            }
        } catch (IOException e) {
            logger.error("Udp send failed", e);
        } catch (InterruptedException e) {
            logger.error("Udp send interrupted", e);
        }
    }

    private void resend() {

    }

    void handleAck(GameMessage message, InetSocketAddress sender) {
        var ack = message.getAck();
        for(var sendData : ackWaiters) {

        }
    }

    @Override
    public void stop() {
        isRunning = false;
    }
}
