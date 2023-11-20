package ru.dyakun.snake.game.timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.game.person.Master;
import ru.dyakun.snake.game.person.Member;
import ru.dyakun.snake.game.tracker.AbstractStatusTracker;
import ru.dyakun.snake.game.tracker.ActiveGamesTracker;
import ru.dyakun.snake.game.event.GameEventListener;
import ru.dyakun.snake.net.NetClient;

import java.net.InetSocketAddress;
import java.util.*;

public class GameTimer {
    private static final Logger logger = LoggerFactory.getLogger(GameTimer.class);
    private static final int TASK_INIT_DELAY = 100;
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

    public void addGameEventListener(GameEventListener listener) {
        listeners.add(listener);
    }

    public void startGameStateUpdate(Master master, int period, NetClient client) {
        if(period < 10) {
            throw new IllegalArgumentException("To little period");
        }
        gameStateTask = new GameStateUpdateTask(master, listeners, client);
        timer.schedule(gameStateTask, TASK_INIT_DELAY, period);
        logger.info("Start game state update");
        announcementTask = new GameAnnouncementTask(client, master, groupAddress);
        logger.info("Start game announcement");
        timer.schedule(announcementTask, TASK_INIT_DELAY, announcementPeriod);
    }

    public void cancelGameStateUpdate() {
        if(gameStateTask == null) {
            return;
        }
        announcementTask.cancel();
        announcementTask = null;
        logger.info("Cancel game announcement");
        gameStateTask.cancel();
        gameStateTask = null;
        logger.info("Cancel game state update");
    }

    public void startPing(InetSocketAddress masterAddress, int period, NetClient client) {
        pingTask = new PingTask(client, masterAddress);
        timer.schedule(pingTask, TASK_INIT_DELAY, period);
        logger.info("Start ping");
    }

    public void changeMasterAddress(InetSocketAddress address) {
        pingTask.setMasterAddress(address);
    }

    public void cancelPing() {
        if(pingTask != null) {
            pingTask.cancel();
            pingTask = null;
            logger.info("Cancel ping");
        }
    }

    public void startPlayersStatusTrack(AbstractStatusTracker tracker, Member member) {
        playersStatusTrackTask = new PlayersStatusTrackTask(tracker, member);
        timer.schedule(playersStatusTrackTask, TASK_INIT_DELAY, tracker.getDeleteTime());
        logger.info("Start players status track");
    }

    public void cancelPlayersStatusTrack() {
        if(playersStatusTrackTask != null) {
            playersStatusTrackTask.cancel();
            playersStatusTrackTask = null;
            logger.info("Cancel players status track");
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
