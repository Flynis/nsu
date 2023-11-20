package ru.dyakun.snake.game.field;

public interface GameField {
    int getWidth();
    int getHeight();
    Tile getTile(int x, int y);
}
