package ru.dyakun.snake.model.timer;

import ru.dyakun.snake.model.util.MessageType;
import ru.dyakun.snake.model.util.MessageUtils;
import ru.dyakun.snake.net.NetClient;

import java.net.InetSocketAddress;
import java.util.TimerTask;

public class PingTask extends TimerTask {
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
        var ping = MessageUtils.pingMessage();
        client.send(MessageType.PING, ping, masterAddress);
    }
}
