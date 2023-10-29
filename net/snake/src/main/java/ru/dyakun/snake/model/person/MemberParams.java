package ru.dyakun.snake.model.person;

import ru.dyakun.snake.controller.SceneManager;
import ru.dyakun.snake.model.event.GameEventListener;
import ru.dyakun.snake.model.timer.GameTimer;
import ru.dyakun.snake.net.NetClient;

import java.util.List;

public record MemberParams(SceneManager manager,
                           NetClient client,
                           GameTimer timer,
                           List<GameEventListener> listeners) {

}
