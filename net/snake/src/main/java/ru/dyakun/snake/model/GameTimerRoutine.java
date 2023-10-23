package ru.dyakun.snake.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.model.field.Field;

import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class GameTimerRoutine extends TimerTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(GameTimerRoutine.class);
    private final ActiveGames activeGames;
    private Field field;
    private final BlockingQueue<Field> pending = new ArrayBlockingQueue<>(2);

    public GameTimerRoutine(ActiveGames activeGames) {
        this.activeGames = activeGames;
    }

    public void changeField(Field field) {
        pending.add(field);
    }

    @Override
    public void run() {
        activeGames.deleteInactiveGames();
        if(!pending.isEmpty()) {
            field = pending.poll();
            logger.debug("Field changed");
        }
        if(field != null) {
            field.updateField();
            logger.debug("Field updated");
        }
    }
}
