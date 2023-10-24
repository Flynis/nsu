package ru.dyakun.snake.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.model.event.GameEvent;
import ru.dyakun.snake.model.event.GameEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GameTimer {
    private static final Logger logger = LoggerFactory.getLogger(GameTimer.class);
    private final Timer timer;
    private final List<GameEventListener> listeners = new ArrayList<>();
    private GameStateTask gameStateTask;

    public GameTimer(ActiveGames activeGames) {
        this.timer = new Timer();
        timer.schedule(new ActiveGamesTask(activeGames), 0, (ActiveGames.DELETE_TIME + 1) * 1000);
    }

    private void notifyListeners() {
        for(var listener : listeners) {
            listener.onEvent(GameEvent.CLEAR_FIELD);
        }
    }

    public void addGameEventListener(GameEventListener listener) {
        listeners.add(listener);
    }

    public void addGameStateTask(GameState state, int period) {
        gameStateTask = new GameStateTask(state, listeners);
        timer.schedule(gameStateTask, 0, period);
    }

    public void cancelGameStateTask() {
        gameStateTask.cancel();
        notifyListeners();
    }

    public void cancel() {
        timer.cancel();
    }

    private static class ActiveGamesTask extends TimerTask {
        private final ActiveGames activeGames;

        private ActiveGamesTask(ActiveGames activeGames) {
            this.activeGames = activeGames;
        }

        @Override
        public void run() {
            activeGames.deleteInactiveGames();
        }
    }

    private static class GameStateTask extends TimerTask {
        private final GameState state;
        private final List<GameEventListener> listeners;

        private GameStateTask(GameState state, List<GameEventListener> listeners) {
            this.state = state;
            this.listeners = listeners;
        }

        private void notifyListeners() {
            for(var listener : listeners) {
                listener.onEvent(GameEvent.REPAINT_FIELD);
            }
        }

        @Override
        public void run() {
            state.update();
            notifyListeners();
            logger.debug("Field updated");
        }
    }
}
