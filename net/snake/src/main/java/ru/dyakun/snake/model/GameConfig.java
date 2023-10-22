package ru.dyakun.snake.model;

import ru.dyakun.snake.util.Config;

public class GameConfig extends Config {
    private int width = 40;
    private int height = 30;
    private int foodStatic = 1;
    private int delay = 1000; // ms

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getFoodStatic() {
        return foodStatic;
    }

    public int getDelay() {
        return delay;
    }

    @Override
    public String toString() {
        return String.format("GameConfig{ field[%dx%d], food = %d, delay = %dms}", width, height, foodStatic, delay);
    }

    @Override
    protected void validate() {
        if(width < 10 || width > 100) {
            throw new IllegalStateException("Illegal width");
        }
        if(height < 10 || height > 100) {
            throw new IllegalStateException("Illegal height");
        }
        if(foodStatic < 0 || foodStatic > 100) {
            throw new IllegalStateException("Illegal food static");
        }
        if(delay < 100 || delay > 3000) {
            throw new IllegalStateException("Illegal delay");
        }
    }
}
