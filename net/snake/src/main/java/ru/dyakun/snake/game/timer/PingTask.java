package ru.dyakun.snake.game.timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.game.util.MessageType;
import ru.dyakun.snake.game.util.Messages;
import ru.dyakun.snake.net.NetClient;

import java.net.InetSocketAddress;
import java.util.TimerTask;

public class PingTask extends TimerTask {
    private static final Logger logger = LoggerFactory.getLogger(PingTask.class);
    private final NetClient client;
    private InetSocketAddress masterAddress;

    public PingTask(NetClient client, InetSocketAddress masterAddress) {
        this.client = client;
        this.masterAddress = masterAddress;
    }

    public void setMasterAddress(InetSocketAddress masterAddress) {
        this.masterAddress = masterAddress;
    }

    @Override
    public void run() {
        try {
            var ping = Messages.pingMessage();
            client.send(MessageType.PING, ping, masterAddress);
        } catch (Exception e) {
            logger.error("Ping failed", e);
        }
    }
}
