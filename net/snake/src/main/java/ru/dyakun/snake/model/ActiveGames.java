package ru.dyakun.snake.model;

import ru.dyakun.snake.net.GameMessageListener;
import ru.dyakun.snake.protocol.GameAnnouncementOrBuilder;
import ru.dyakun.snake.protocol.GameMessageOrBuilder;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ActiveGames implements GameMessageListener {
    private final Map<GameAnnouncementOrBuilder, LocalDateTime> games = new HashMap<>();

    public Collection<GameAnnouncementOrBuilder> getActiveGames() {
        return games.keySet();
    }

    void deleteInactiveGames() {
        // delete if > 2s
        games.entrySet().removeIf(item -> ChronoUnit.SECONDS.between(item.getValue(), LocalDateTime.now()) > 2);
    }

    @Override
    public void handle(GameMessageOrBuilder message, InetAddress receiver) {
        // TODO handle
    }
}
