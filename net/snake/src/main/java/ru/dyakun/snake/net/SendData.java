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

    SendData(MessageType type, GameMessage message, InetSocketAddress receiver) {
        this.type = type;
        this.message = message;
        this.receiver = receiver;
        this.sendTime = null;
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

    public void setReceiver(InetSocketAddress receiver) {
        this.receiver = receiver;
    }

    public void setSendTime(LocalDateTime sendTime) {
        this.sendTime = sendTime;
    }
}
