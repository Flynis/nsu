package ru.dyakun.snake.game.field;

import ru.dyakun.snake.game.entity.Point;
import ru.dyakun.snake.game.entity.Snake;

import java.util.Collection;

public class StaticField extends AbstractField {
    public StaticField(int width, int height) {
        super(width, height);
    }

    public void fillBy(Collection<Snake> snakes, Collection<Point> foods) {
        fill(snakes, foods);
    }
}
