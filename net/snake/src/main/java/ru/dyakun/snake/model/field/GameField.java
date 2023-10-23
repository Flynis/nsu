package ru.dyakun.snake.model.field;

public interface GameField {
    int getWidth();
    int getHeight();
    Tile getTile(int x, int y);
}
