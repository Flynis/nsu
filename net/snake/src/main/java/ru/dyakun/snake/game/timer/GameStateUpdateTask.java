package ru.dyakun.snake.game.timer;

import ru.dyakun.snake.game.event.GameEvent;
import ru.dyakun.snake.game.event.GameEventListener;
import ru.dyakun.snake.game.person.Master;
import ru.dyakun.snake.game.util.MessageType;
import ru.dyakun.snake.game.util.Messages;
import ru.dyakun.snake.net.NetClient;

import java.util.List;
import java.util.TimerTask;

public class GameStateUpdateTask extends TimerTask {
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
        master.update();
        var state = master.getState();
        var players = state.getPlayers();
        var message = Messages.stateMessage(state);
        for(var player : players) {
            client.send(MessageType.STATE, message, player.getAddress());
        }
        notifyListeners();
    }
}
