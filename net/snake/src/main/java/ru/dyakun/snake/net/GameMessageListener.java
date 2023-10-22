package ru.dyakun.snake.net;

import ru.dyakun.snake.protocol.GameMessageOrBuilder;

import java.net.InetAddress;

public interface GameMessageListener {
    void handle(GameMessageOrBuilder message, InetAddress receiver);
}
