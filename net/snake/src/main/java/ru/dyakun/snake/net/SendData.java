package ru.dyakun.snake.net;

import ru.dyakun.snake.game.util.MessageType;
import ru.dyakun.snake.protocol.GameMessage;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;

public class SendData {
    private final MessageType type;
    private final GameMessage message;
    private InetSocketAddress receiver;
    private LocalDateTime sendTime;
    private int sendCount;

    SendData(MessageType type, GameMessage message, InetSocketAddress receiver) {
        this.type = type;
        this.message = message;
        this.receiver = receiver;
        this.sendTime = null;
        this.sendCount = 0;
    }

    public MessageType type() {
        return type;
    }

    public GameMessage message() {
        return message;
    }

    public InetSocketAddress receiver() {
        return receiver;
    }

    public LocalDateTime sendTime() {
        return sendTime;
    }

    public int sendCount() {
        return sendCount;
    }

    public void setReceiver(InetSocketAddress receiver) {
        this.receiver = receiver;
    }

    public void setSendTime(LocalDateTime sendTime) {
        this.sendTime = sendTime;
    }

    public void incSendCount() {
        sendCount++;
    }

    @Override
    public String toString() {
        return String.format("SendData{%s[%d %d] to %s:%d}",
                type,
                message.getMsgSeq(),
                sendCount,
                receiver.getAddress().getHostAddress(),
                receiver.getPort());
    }
}
