package ru.dyakun.paint.painter;

import ru.dyakun.paint.Canvas;

import javax.swing.*;

public class StampPainter extends Painter {

    private int n = 4;
    private int radius = 1;
    private int angle = 0;

    public void setN(int n) {
        this.n = n;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    public StampPainter(Canvas canvas, JFrame frame) {
        super(canvas, frame);
    }

}
