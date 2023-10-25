package ru.dyakun.snake.net;

import ru.dyakun.snake.protocol.GameMessage;

import java.net.InetSocketAddress;

public interface GameMessageListener {
    void handle(GameMessage message, InetSocketAddress sender);
}
