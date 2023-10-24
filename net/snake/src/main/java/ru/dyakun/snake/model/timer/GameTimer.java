package ru.dyakun.snake.model.timer;

import ru.dyakun.snake.model.ActiveGames;
import ru.dyakun.snake.model.GameState;
import ru.dyakun.snake.model.event.GameEvent;
import ru.dyakun.snake.model.event.GameEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GameTimer {
    private final Timer timer;
    private final List<GameEventListener> listeners = new ArrayList<>();
    private GameStateUpdateTask gameStateTask;
    private final int announcementPeriod;

    public GameTimer(ActiveGames activeGames, int announcementPeriod) {
        if (announcementPeriod < 10) {
            throw new IllegalArgumentException("Announcement period is too little");
        }
        this.announcementPeriod = announcementPeriod;
        this.timer = new Timer();
        int period = activeGames.getAnnouncementTimeToLive();
        timer.schedule(new ActiveGamesTask(activeGames), period, period);
    }

    private void notifyListeners() {
        for(var listener : listeners) {
            listener.onEvent(GameEvent.CLEAR_FIELD);
        }
    }

    public void addGameEventListener(GameEventListener listener) {
        listeners.add(listener);
    }

    public void startGameStateUpdate(GameState state, int period) {
        if(period < 10) {
            throw new IllegalArgumentException("To little period");
        }
        gameStateTask = new GameStateUpdateTask(state, listeners);
        timer.schedule(gameStateTask, 0, period);
    }

    public void startAnnouncementSend() {
        // TODO impl
    }

    public void cancelAnnouncementSend() {

    }

    public void cancelGameStateUpdate() {
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
}
