package ru.dyakun.snake.model.timer;

import ru.dyakun.snake.model.GameState;
import ru.dyakun.snake.model.util.MessageType;
import ru.dyakun.snake.model.util.MessageUtils;
import ru.dyakun.snake.net.NetClient;

import java.net.InetSocketAddress;
import java.util.TimerTask;

public class GameAnnouncementTask extends TimerTask {
    private final NetClient client;
    private final GameState state;
    private final InetSocketAddress group;

    public GameAnnouncementTask(NetClient client, GameState state, InetSocketAddress group) {
        this.client = client;
        this.state = state;
        this.group = group;
    }

    @Override
    public void run() {
        var message = MessageUtils.announcementMessage(state.getGameInfo());
        client.send(MessageType.ANNOUNCEMENT, message, group);
    }
}
