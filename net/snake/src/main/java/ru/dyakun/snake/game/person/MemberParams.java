package ru.dyakun.snake.game.person;

import ru.dyakun.snake.game.event.GameEventListener;
import ru.dyakun.snake.game.timer.GameTimer;
import ru.dyakun.snake.net.NetClient;

import java.util.List;

public record MemberParams(NetClient client,
                           GameTimer timer,
                           List<GameEventListener> listeners) {

}
