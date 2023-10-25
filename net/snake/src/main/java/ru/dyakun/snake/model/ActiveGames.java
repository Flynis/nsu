package ru.dyakun.snake.model;

import ru.dyakun.snake.model.event.GameEvent;
import ru.dyakun.snake.model.event.GameEventListener;
import ru.dyakun.snake.net.GameMessageListener;
import ru.dyakun.snake.protocol.GameAnnouncement;
import ru.dyakun.snake.protocol.GameMessage;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ActiveGames implements GameMessageListener {
    private record Entry(GameInfo game, LocalDateTime time) {}
    private final Map<String, Entry> games = new ConcurrentHashMap<>();
    private final List<GameEventListener> listeners = new ArrayList<>();
    private final int announcementTimeToLive;

    public ActiveGames(int announcementTimeToLive) {
        if(announcementTimeToLive < 1000) {
            throw new IllegalArgumentException("To little announcement time to live");
        }
        this.announcementTimeToLive = announcementTimeToLive;
    }

    public int getAnnouncementTimeToLive() {
        return announcementTimeToLive;
    }

    private void notifyListeners() {
        for(var listener : listeners) {
            listener.onEvent(GameEvent.UPDATE_ACTIVE_GAMES);
        }
    }

    public void addGameEventListener(GameEventListener listener) {
        listeners.add(listener);
    }

    public List<GameInfoView> getActiveGames() {
        return games.values().stream().map(e -> (GameInfoView)e.game).toList();
    }

    public GameInfo getByName(String name) {
        return games.get(name).game;
    }

    public void deleteInactiveGames() {
        games.entrySet().removeIf(
                item -> ChronoUnit.MILLIS.between(item.getValue().time, LocalDateTime.now()) > announcementTimeToLive);
        notifyListeners();
    }

    @Override
    public void handle(GameMessage message, InetSocketAddress sender) {
        if(message.hasAnnouncement()) {
            GameMessage.AnnouncementMsg announcementMsg = message.getAnnouncement();
            GameAnnouncement announcement = announcementMsg.getGames(0);
            GameInfo gameInfo = GameInfo.fromAnnouncement(announcement);
            if(games.containsKey(gameInfo.getName())) {
                games.get(gameInfo.getName()).game.updateFrom(gameInfo);
            } else {
                games.put(gameInfo.getName(), new Entry(gameInfo, LocalDateTime.now()));
            }
            gameInfo.findMaster().setAddress(sender);
            notifyListeners();
        }
    }
}
