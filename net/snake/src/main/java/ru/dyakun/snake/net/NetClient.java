package ru.dyakun.snake.net;

import ru.dyakun.snake.model.util.MessageType;
import ru.dyakun.snake.protocol.GameMessage;

import java.net.InetSocketAddress;

public interface NetClient extends MessageReceiver {
    void send(MessageType type, GameMessage message, InetSocketAddress receiver);

    void changeReceiver(InetSocketAddress current, InetSocketAddress old);

    void setTimeout(int timeout);
}
