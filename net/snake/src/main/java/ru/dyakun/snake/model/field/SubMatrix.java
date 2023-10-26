package ru.dyakun.snake.model.field;

import ru.dyakun.snake.model.entity.Point;

public record SubMatrix(Point leftUpper, Point bottomRight) {
    public SubMatrix {
        if(leftUpper.x < 0 || leftUpper.y < 0) {
            throw new IllegalArgumentException("Point coords must be positive");
        }
        if(bottomRight.x < 0 || bottomRight.y < 0) {
            throw new IllegalArgumentException("Point coords must be positive");
        }
    }

    public int getSize() {
        return Math.min(bottomRight.x - leftUpper.x, bottomRight.y - leftUpper.y);
    }

    @Override
    public String toString() {
        return String.format("SubMatrix{ n = %d, (%d, %d), (%d, %d)}",
                getSize(),
                leftUpper.x,
                leftUpper.y,
                bottomRight.x,
                bottomRight.y);
    }
}
