package ru.dyakun.snake.game.tracker;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayersStatusTracker extends AbstractStatusTracker {
    private final Map<Integer, LocalDateTime> statuses = new ConcurrentHashMap<>();

    public PlayersStatusTracker(int deleteTime) {
        super(deleteTime);
    }

    @Override
    public void updateStatus(int id) {
        statuses.put(id, LocalDateTime.now());
    }

    @Override
    public Collection<Integer> deleteInactive() {
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
