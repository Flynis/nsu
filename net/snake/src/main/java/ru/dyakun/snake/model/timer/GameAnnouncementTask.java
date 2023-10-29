package ru.dyakun.snake.model.timer;

import ru.dyakun.snake.model.GameState;
import ru.dyakun.snake.model.person.Master;
import ru.dyakun.snake.model.util.MessageType;
import ru.dyakun.snake.model.util.Messages;
import ru.dyakun.snake.net.NetClient;

import java.net.InetSocketAddress;
import java.util.TimerTask;

public class GameAnnouncementTask extends TimerTask {
    private final NetClient client;
    private final InetSocketAddress group;
    private final Master master;

    public GameAnnouncementTask(NetClient client, Master master, InetSocketAddress group) {
        this.client = client;
        this.master = master;
        this.group = group;
    }

    @Override
    public void run() {
        var message = Messages.announcementMessage(master.getGameInfo());
        client.send(MessageType.ANNOUNCEMENT, message, group);
    }
}
