package ru.dyakun.snake.model.tracker;

import java.util.Collection;

public abstract class AbstractStatusTracker {
    protected final int deleteTime;

    protected AbstractStatusTracker(int deleteTime) {
        if(deleteTime < 1) {
            throw new IllegalArgumentException("Illegal time to delete");
        }
        this.deleteTime = deleteTime;
    }

    public int getDeleteTime() {
        return deleteTime;
    }

    public abstract void updateStatus(int id);

    public abstract Collection<Integer> deleteInactive();
}
