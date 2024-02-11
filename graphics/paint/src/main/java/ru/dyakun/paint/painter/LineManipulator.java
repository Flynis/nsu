package ru.dyakun.paint.painter;

import ru.dyakun.paint.Canvas;

import javax.swing.*;

public class LineManipulator extends Manipulator {

    private int x0;
    private int y0;
    private int strokeWidth;

    public void setX0(int x0) {
        this.x0 = x0;
    }

    public void setY0(int y0) {
        this.y0 = y0;
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public LineManipulator(Canvas canvas, JFrame frame) {
        super(canvas, frame);
    }

}
