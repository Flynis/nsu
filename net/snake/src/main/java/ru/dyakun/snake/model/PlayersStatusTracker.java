package ru.dyakun.snake.model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayersStatusTracker {
    private final Map<Integer, LocalDateTime> statuses = new ConcurrentHashMap<>();
    private final int deleteTime;

    public PlayersStatusTracker(int deleteTime) {
        if(deleteTime < 1) {
            throw new IllegalArgumentException("Illegal time to delete");
        }
        this.deleteTime = deleteTime;
    }

    public int getDeleteTime() {
        return deleteTime;
    }

    public void updateStatus(int playerId) {
        statuses.put(playerId, LocalDateTime.now());
    }

    public Collection<Integer> deleteInactivePlayers() {
        Collection<Integer> deleted = new ArrayList<>();
        for(var entry : statuses.entrySet()) {
            if(ChronoUnit.MILLIS.between(entry.getValue(), LocalDateTime.now()) > deleteTime) {
                deleted.add(entry.getKey());
            }
        }
        for(var id: deleted) {
            statuses.remove(id);
        }
        return deleted;
    }
}
