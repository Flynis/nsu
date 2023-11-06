package ru.dyakun.snake.game.tracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.game.entity.GameInfo;
import ru.dyakun.snake.game.event.GameEvent;
import ru.dyakun.snake.game.event.GameEventListener;
import ru.dyakun.snake.net.GameMessageListener;
import ru.dyakun.snake.protocol.GameAnnouncement;
import ru.dyakun.snake.protocol.GameMessage;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ActiveGamesTracker implements GameMessageListener {
    private static final Logger logger = LoggerFactory.getLogger(ActiveGamesTracker.class);
    private final Map<String, Entry> games = new ConcurrentHashMap<>();
    private final List<GameEventListener> listeners = new ArrayList<>();
    private final int announcementTimeToLive;

    public ActiveGamesTracker(int announcementTimeToLive) {
        if(announcementTimeToLive < 1000) {
            throw new IllegalArgumentException("To little announcement time to live");
        }
        this.announcementTimeToLive = announcementTimeToLive;
    }

    public int getAnnouncementTimeToLive() {
        return announcementTimeToLive;
    }

    private void notifyListeners(GameEvent event, Object payload) {
        logger.info("Active games notify {}", event);
        for(var listener : listeners) {
            listener.onEvent(event, payload);
        }
    }

    public void addGameEventListener(GameEventListener listener) {
        listeners.add(listener);
    }

    public GameInfo getByName(String name) {
        return games.get(name).getGame();
    }

    public void deleteInactiveGames() {
        Collection<String> inactive = new ArrayList<>();
        for(var entry: games.values()) {
            if(ChronoUnit.MILLIS.between(entry.getTime(), LocalDateTime.now()) > announcementTimeToLive) {
                inactive.add(entry.game.getName());
            }
        }
        for(var name: inactive) {
            games.remove(name);
            notifyListeners(GameEvent.DELETE_INACTIVE_GAMES, name);
        }
    }

    @Override
    public void handle(GameMessage message, InetSocketAddress sender) {
        if(message.hasAnnouncement()) {
            logger.debug("Receive ANNOUNCEMENT");
            GameMessage.AnnouncementMsg announcementMsg = message.getAnnouncement();
            GameAnnouncement announcement = announcementMsg.getGames(0);
            GameInfo gameInfo = GameInfo.fromAnnouncement(announcement);
            gameInfo.findMaster().setAddress(sender);
            if(games.containsKey(gameInfo.getName())) {
                games.get(gameInfo.getName()).update(gameInfo);
                notifyListeners(GameEvent.UPDATE_GAME_INFO, gameInfo);
            } else {
                games.put(gameInfo.getName(), new Entry(gameInfo));
                notifyListeners(GameEvent.NEW_ACTIVE_GAME, gameInfo);
            }
        }
    }

    private static class Entry {
        private final GameInfo game;
        private LocalDateTime time;

        private Entry(GameInfo game) {
            this.game = game;
            time = LocalDateTime.now();
        }

        public GameInfo getGame() {
            return game;
        }

        public LocalDateTime getTime() {
            return time;
        }

        public void update(GameInfo gameInfo) {
            gameInfo.updateBy(gameInfo);
            time = LocalDateTime.now();
        }
    }
}
