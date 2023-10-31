package ru.dyakun.snake.game.tracker;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;

public class MasterStatusTracker extends AbstractStatusTracker {
    private int masterId;
    private LocalDateTime time;

    public MasterStatusTracker(int deleteTime, int id) {
        super(deleteTime);
        masterId = id;
        time = LocalDateTime.now();
    }

    public void changeMasterId(int id) {
        masterId = id;
    }

    @Override
    public void updateStatus(int id) {
        if(id == masterId) {
            time = LocalDateTime.now();
        }
    }

    @Override
    public Collection<Integer> deleteInactive() {
        Collection<Integer> deleted = new ArrayList<>();
        if(ChronoUnit.MILLIS.between(time, LocalDateTime.now()) > deleteTime) {
            deleted.add(masterId);
        }
        return deleted;
    }
}
