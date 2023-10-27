package ru.dyakun.snake.model.timer;

import ru.dyakun.snake.model.ActiveGames;
import ru.dyakun.snake.model.GameState;
import ru.dyakun.snake.model.event.GameEvent;
import ru.dyakun.snake.model.event.GameEventListener;
import ru.dyakun.snake.net.NetClient;

import java.net.InetSocketAddress;
import java.util.*;

public class GameTimer {
    private final Timer timer;
    private final List<GameEventListener> listeners = new ArrayList<>();
    private GameStateUpdateTask gameStateTask;
    private GameAnnouncementTask announcementTask;
    private PingTask pingTask;
    private final int announcementPeriod;
    private final InetSocketAddress groupAddress;

    public GameTimer(ActiveGames activeGames, int announcementPeriod, InetSocketAddress groupAddress) {
        if (announcementPeriod < 10) {
            throw new IllegalArgumentException("Announcement period is too little");
        }
        this.groupAddress = groupAddress;
        this.announcementPeriod = announcementPeriod;
        this.timer = new Timer();
        int period = activeGames.getAnnouncementTimeToLive();
        timer.schedule(new ActiveGamesTask(activeGames), period, period);
    }

    private void notifyListeners() {
        for(var listener : listeners) {
            listener.onEvent(GameEvent.CLEAR_FIELD, null);
        }
    }

    public void addGameEventListener(GameEventListener listener) {
        listeners.add(listener);
    }

    public void startGameStateUpdate(GameState state, int period, NetClient client) {
        if(period < 10) {
            throw new IllegalArgumentException("To little period");
        }
        gameStateTask = new GameStateUpdateTask(state, listeners, client);
        timer.schedule(gameStateTask, 0, period);
        announcementTask = new GameAnnouncementTask(client, state, groupAddress);
        timer.schedule(announcementTask, 0, announcementPeriod);
    }

    public void cancelGameStateUpdate() {
        if(gameStateTask == null) {
            return;
        }
        announcementTask.cancel();
        announcementTask = null;
        gameStateTask.cancel();
        gameStateTask = null;
        notifyListeners();
    }

    public void startPing(InetSocketAddress masterAddress, int period, NetClient client) {
        pingTask = new PingTask(client, masterAddress);
        timer.schedule(pingTask, 0, period);
    }

    public void changeMasterAddress(InetSocketAddress address) {
        pingTask.setMasterAddress(address);
    }

    public void cancelPing() {
        if(pingTask != null) {
            pingTask.cancel();
            pingTask = null;
        }
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
