package ru.dyakun.snake.model.timer;

import ru.dyakun.snake.model.GameState;
import ru.dyakun.snake.model.util.MessageUtils;
import ru.dyakun.snake.net.NetClient;

import java.util.TimerTask;

public class GameAnnouncementTask extends TimerTask {
    private final NetClient client;
    private final GameState state;

    public GameAnnouncementTask(NetClient client, GameState state) {
        this.client = client;
        this.state = state;
    }

    @Override
    public void run() {
        var message = MessageUtils.announcementFrom(state.getCurrentGameInfo());
        // TODO impl
    }
}
