package ru.dyakun.snake.game.timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dyakun.snake.game.event.GameEvent;
import ru.dyakun.snake.game.event.GameEventListener;
import ru.dyakun.snake.game.person.Master;
import ru.dyakun.snake.game.util.MessageType;
import ru.dyakun.snake.net.NetClient;
import ru.dyakun.snake.protocol.NodeRole;

import java.util.List;
import java.util.TimerTask;

public class GameStateUpdateTask extends TimerTask {
    private static final Logger logger = LoggerFactory.getLogger(GameStateUpdateTask.class);
    private final List<GameEventListener> listeners;
    private final NetClient client;
    private final Master master;

    GameStateUpdateTask(Master master, List<GameEventListener> listeners, NetClient client) {
        this.master = master;
        this.listeners = listeners;
        this.client = client;
    }

    private void notifyListeners() {
        for(var listener : listeners) {
            listener.onEvent(GameEvent.REPAINT, null);
        }
    }

    @Override
    public void run() {
        try {
            master.update();
            var state = master.getState();
            var players = state.getPlayers();
            var message = master.getStateMessage();
            for(var player : players) {
                if(player.getRole() != NodeRole.MASTER) {
                    client.send(MessageType.STATE, message, player.getAddress());
                }
            }
            notifyListeners();
        } catch (NullPointerException e) {
            logger.error("State update failed", e);
        } catch (Exception e) {
            logger.error("State update failed {}", e.getMessage(), e);
        }
    }
}
