package ru.dyakun.paint.painter;

import ru.dyakun.paint.Canvas;
import ru.dyakun.paint.IntegerProperty;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class LineManipulator extends Manipulator {

    private int x0;
    private int y0;
    private final IntegerProperty strokeWidth;
    private boolean firstClick = true;

    public LineManipulator(Canvas canvas, JFrame frame) {
        super(canvas, frame);
        strokeWidth = new IntegerProperty(1, 1, 36, "Stroke width");
        dialog.initByProperty(strokeWidth);
    }

    @Override
    public void reset() {
        firstClick = true;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if(firstClick) {
            firstClick = false;
            x0 = e.getX();
            y0 = e.getY();
        } else {
            int x = e.getX();
            int y = e.getY();
            if(strokeWidth.getVal() == 1) {
                drawBresenhamLine(canvas, x0, y0, x, y);
            } else {
                Graphics2D g = (Graphics2D) canvas.getImage().getGraphics();
                g.setStroke(new BasicStroke(strokeWidth.getVal()));
                g.setColor(canvas.getColor());
                g.drawLine(x0, y0, x, y);
            }
            x0 = x;
            y0 = y;
            canvas.repaint();
        }
    }

    private void drawBresenhamLine(Canvas canvas, int x0, int y0, int x1, int y1) {

    }

}
