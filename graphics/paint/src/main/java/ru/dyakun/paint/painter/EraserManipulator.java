package ru.dyakun.paint.painter;

import ru.dyakun.paint.Canvas;
import ru.dyakun.paint.IntegerProperty;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class EraserManipulator extends Manipulator {

    private final IntegerProperty radius;

    public EraserManipulator(Canvas canvas, JFrame frame) {
        super(canvas, frame);
        radius = new IntegerProperty(1, 1, 100, "Radius");
        dialog.initByProperty(radius);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        erase(e.getX(), e.getY());
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        erase(e.getX(), e.getY());
    }

    private void erase(int x, int y) {
        int r = radius.getVal();
        Graphics2D g = (Graphics2D) canvas.getImage().getGraphics();
        g.setColor(Canvas.BACKGROUND_COLOR);
        g.fillOval(x - r, y - r , r * 2, r * 2);
        canvas.repaint();
    }

}
