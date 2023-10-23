package ru.dyakun.snake.model;

import ru.dyakun.snake.model.event.GameEvent;
import ru.dyakun.snake.model.event.GameEventListener;
import ru.dyakun.snake.net.GameMessageListener;
import ru.dyakun.snake.protocol.GameAnnouncement;
import ru.dyakun.snake.protocol.GameMessage;

import java.net.SocketAddress;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActiveGames implements GameMessageListener {
    private static final int DELETE_TIME = 2; // sec
    private record Entry(GameInfo game, LocalDateTime time) {}
    private final Map<String, Entry> games = new HashMap<>();
    private final List<GameEventListener> listeners = new ArrayList<>();

    private void notifyListeners(GameEvent event) {
        for(var listener : listeners) {
            listener.onEvent(event);
        }
    }

    public void addGameEventListener(GameEventListener listener) {
        listeners.add(listener);
    }

    public List<GameInfo> getActiveGames() {
        return games.values().stream().map(e -> e.game).toList();
    }

    void deleteInactiveGames() {
        games.entrySet().removeIf(item -> ChronoUnit.SECONDS.between(item.getValue().time, LocalDateTime.now()) > DELETE_TIME);
    }

    @Override
    public void handle(GameMessage message, SocketAddress receiver) {
        if(message.hasAnnouncement()) {
            GameMessage.AnnouncementMsg announcementMsg = message.getAnnouncement();
            GameAnnouncement announcement = announcementMsg.getGames(0);
            GameInfo gameInfo = GameInfo.fromAnnouncement(announcement);
            if(games.containsKey(gameInfo.getName())) {
                games.get(gameInfo.getName()).game.updateFrom(gameInfo);
            } else {
                games.put(gameInfo.getName(), new Entry(gameInfo, LocalDateTime.now()));
            }
        }
    }
}
