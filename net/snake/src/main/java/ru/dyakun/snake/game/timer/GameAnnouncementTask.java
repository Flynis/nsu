package ru.dyakun.snake.game.timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.Launcher;
import ru.dyakun.snake.game.person.Master;
import ru.dyakun.snake.game.util.MessageType;
import ru.dyakun.snake.game.util.Messages;
import ru.dyakun.snake.net.NetClient;

import java.net.InetSocketAddress;
import java.util.TimerTask;

public class GameAnnouncementTask extends TimerTask {
    private static final Logger logger = LoggerFactory.getLogger(GameAnnouncementTask.class);
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
        try {
            var message = Messages.announcementMessage(master.getGameInfo());
            client.send(MessageType.ANNOUNCEMENT, message, group);
        } catch (Exception e) {
            logger.error("Game announcement failed", e);
        }
    }
}
