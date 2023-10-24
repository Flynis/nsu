package ru.dyakun.snake.model.timer;

import ru.dyakun.snake.model.GameState;
import ru.dyakun.snake.model.event.GameEvent;
import ru.dyakun.snake.model.event.GameEventListener;

import java.util.List;
import java.util.TimerTask;

public class GameStateUpdateTask extends TimerTask {
    private final GameState state;
    private final List<GameEventListener> listeners;

    GameStateUpdateTask(GameState state, List<GameEventListener> listeners) {
        this.state = state;
        this.listeners = listeners;
    }

    private void notifyListeners() {
        for(var listener : listeners) {
            listener.onEvent(GameEvent.REPAINT_FIELD);
        }
    }

    @Override
    public void run() {
        state.update();
        notifyListeners();
    }
}
