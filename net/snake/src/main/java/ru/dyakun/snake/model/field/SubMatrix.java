package ru.dyakun.snake.model.field;

import ru.dyakun.snake.model.entity.Point;

public record SubMatrix(int n, Point leftUpper, Point bottomRight) {
    public SubMatrix {
        if (n < 0) {
            throw new IllegalArgumentException("Sub matrix size must be positive");
        }
        if(leftUpper.x < 0 || leftUpper.y < 0) {
            throw new IllegalArgumentException("Point coords must be positive");
        }
        if(bottomRight.x < 0 || bottomRight.y < 0) {
            throw new IllegalArgumentException("Point coords must be positive");
        }
    }

    @Override
    public String toString() {
        return String.format("SubMatrix{ n = %d, (%d, %d), (%d, %d)}",
                n,
                leftUpper.x,
                leftUpper.y,
                bottomRight.x,
                bottomRight.y);
    }
}
