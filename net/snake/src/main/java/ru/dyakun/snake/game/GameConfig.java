package ru.dyakun.snake.game;

public class GameConfig {
    private final int width;
    private final int height;
    private final int foodStatic;
    private final int delay; // ms

    private GameConfig(int width, int height, int foodStatic, int delay) {
        this.width = width;
        this.height = height;
        this.foodStatic = foodStatic;
        this.delay = delay;
    }

    public static GameConfig from(ru.dyakun.snake.protocol.GameConfig config) {
        return new Builder()
                .width(config.getWidth())
                .height(config.getHeight())
                .food(config.getFoodStatic())
                .delay(config.getStateDelayMs())
                .build();
    }

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

    private static void validateWidth(int width) {
        if(width < 10 || width > 100) {
            throw new IllegalStateException("Illegal width");
        }
    }

    private static void validateHeight(int height) {
        if(height < 10 || height > 100) {
            throw new IllegalStateException("Illegal height");
        }
    }

    private static void validateFood(int foodStatic) {
        if(foodStatic < 0 || foodStatic > 100) {
            throw new IllegalStateException("Illegal food static");
        }
    }

    private static void validateDelay(int delay) {
        if(delay < 100 || delay > 3000) {
            throw new IllegalStateException("Illegal delay");
        }
    }

    public static class Builder {
        private int width;
        private int height;
        private int foodStatic;
        private int delay; // ms

        public Builder() {
            this.width = 10;
            this.height = 10;
            this.foodStatic = 1;
            this.delay = 1000;
        }

        public Builder width(int width) {
            validateWidth(width);
            this.width = width;
            return this;
        }

        public Builder height(int height) {
            validateHeight(height);
            this.height = height;
            return this;
        }

        public Builder food(int foodStatic) {
            validateFood(foodStatic);
            this.foodStatic = foodStatic;
            return this;
        }

        public Builder delay(int delay) {
            validateDelay(delay);
            this.delay = delay;
            return this;
        }

        public GameConfig build() {
            return new GameConfig(width, height, foodStatic, delay);
        }
    }
}
