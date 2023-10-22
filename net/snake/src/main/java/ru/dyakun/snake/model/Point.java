package ru.dyakun.snake.model;

class Point {
    public int x;
    public int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object object) {
        if(object instanceof Point p) {
            return this.x == p.x && this.y == p.y;
        } else {
            return false;
        }
    }
}
