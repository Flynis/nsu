package ru.dyakun.snake.model.timer;

import ru.dyakun.snake.model.person.Master;
import ru.dyakun.snake.model.person.Member;
import ru.dyakun.snake.model.tracker.AbstractStatusTracker;
import ru.dyakun.snake.model.tracker.ActiveGamesTracker;
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
    private PlayersStatusTrackTask playersStatusTrackTask;
    private PingTask pingTask;
    private final int announcementPeriod;
    private final InetSocketAddress groupAddress;

    public GameTimer(ActiveGamesTracker activeGamesTracker, int announcementPeriod, InetSocketAddress groupAddress) {
        if (announcementPeriod < 10) {
            throw new IllegalArgumentException("Announcement period is too little");
        }
        this.groupAddress = groupAddress;
        this.announcementPeriod = announcementPeriod;
        this.timer = new Timer();
        int period = activeGamesTracker.getAnnouncementTimeToLive();
        timer.schedule(new ActiveGamesTask(activeGamesTracker), period, period);
    }

    private void notifyListeners() {
        for(var listener : listeners) {
            listener.onEvent(GameEvent.CLEAR_FIELD, null);
        }
    }

    public void addGameEventListener(GameEventListener listener) {
        listeners.add(listener);
    }

    public void startGameStateUpdate(Master master, int period, NetClient client) {
        if(period < 10) {
            throw new IllegalArgumentException("To little period");
        }
        gameStateTask = new GameStateUpdateTask(master, listeners, client);
        timer.schedule(gameStateTask, 0, period);
        announcementTask = new GameAnnouncementTask(client, master, groupAddress);
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

    public void startPlayersStatusTrack(AbstractStatusTracker tracker, Member member) {
        playersStatusTrackTask = new PlayersStatusTrackTask(tracker, member);
        timer.schedule(playersStatusTrackTask, 0, tracker.getDeleteTime());
    }

    public void cancelPlayersStatusTrack() {
        if(playersStatusTrackTask != null) {
            playersStatusTrackTask.cancel();
            playersStatusTrackTask = null;
        }
    }

    public void cancel() {
        timer.cancel();
    }

    private static class ActiveGamesTask extends TimerTask {
        private final ActiveGamesTracker activeGamesTracker;

        private ActiveGamesTask(ActiveGamesTracker activeGamesTracker) {
            this.activeGamesTracker = activeGamesTracker;
        }

        @Override
        public void run() {
            activeGamesTracker.deleteInactiveGames();
        }
    }
}
