package ru.dyakun.paint.painter;

import ru.dyakun.paint.Canvas;
import ru.dyakun.paint.IntegerProperty;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class PencilManipulator extends Manipulator {

    private int x0;
    private int y0;
    private final IntegerProperty strokeWidth;
    private boolean mousePressed = false;

    public PencilManipulator(Canvas canvas, JFrame frame) {
        super(canvas, frame);
        strokeWidth = new IntegerProperty(1, 1, 36, "Stroke width");
        dialog.initByProperty(strokeWidth);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mousePressed = true;
        x0 = e.getX();
        y0 = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mousePressed = false;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if(mousePressed) {
            int x = e.getX();
            int y = e.getY();
            Graphics2D g = (Graphics2D) canvas.getImage().getGraphics();
            g.setStroke(new BasicStroke(strokeWidth.getVal()));
            g.setColor(canvas.getColor());
            g.drawLine(x0, y0, x, y);
            x0 = x;
            y0 = y;
            canvas.repaint();
        }
    }

}
