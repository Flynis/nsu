package ru.dyakun.paint.painter;

import ru.dyakun.paint.Canvas;
import ru.dyakun.paint.IntegerProperty;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.List;

public class StampManipulator extends Manipulator {

    private int n;
    private final IntegerProperty radius;
    private final IntegerProperty theta;

    public void setN(int n) {
        this.n = n;
    }

    public StampManipulator(Canvas canvas, JFrame frame) {
        super(canvas, frame);
        radius = new IntegerProperty(1, 1, 100, "Radius");
        theta = new IntegerProperty(0, 0, 360, "Rotate");
        dialog.initByProperty(List.of(radius, theta));
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
    }

}
