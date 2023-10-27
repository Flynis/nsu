package ru.dyakun.snake.model.timer;

import ru.dyakun.snake.model.GameState;
import ru.dyakun.snake.model.event.GameEvent;
import ru.dyakun.snake.model.event.GameEventListener;
import ru.dyakun.snake.model.util.MessageType;
import ru.dyakun.snake.model.util.MessageUtils;
import ru.dyakun.snake.net.NetClient;

import java.util.List;
import java.util.TimerTask;

public class GameStateUpdateTask extends TimerTask {
    private final GameState state;
    private final List<GameEventListener> listeners;
    private final NetClient client;

    GameStateUpdateTask(GameState state, List<GameEventListener> listeners, NetClient client) {
        this.state = state;
        this.listeners = listeners;
        this.client = client;
    }

    private void notifyListeners() {
        for(var listener : listeners) {
            listener.onEvent(GameEvent.REPAINT_FIELD, null);
        }
    }

    @Override
    public void run() {
        state.update();
        var players = state.getMembers();
        var message = MessageUtils.stateMessage(state);
        for(var player : players) {
            client.send(MessageType.STATE, message, player.getAddress());
        }
        notifyListeners();
    }
}
