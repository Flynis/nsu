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
            Graphics2D g = (Graphics2D) canvas.getImage().getGraphics();
            g.setStroke(new BasicStroke(strokeWidth.getVal()));
            g.setColor(canvas.getColor());
            if(strokeWidth.getVal() == 1) {
                drawBresenhamLine(g, x0, y0, x, y);
            } else {
                g.drawLine(x0, y0, x, y);
            }
            x0 = x;
            y0 = y;
            canvas.repaint();
        }
    }

    public static void drawBresenhamLine(Graphics g, int x0, int y0, int x1, int y1) {
        int dx = x1 - x0;
        int dy = y1 - y0;
        int signX = Integer.compare(dx, 0);
        int signY = Integer.compare(dy, 0);
        if (dx < 0) {
            dx = -dx;
        }
        if (dy < 0) {
            dy = -dy;
        }
        int pdx, pdy, es, el;
        if (dx > dy) {
            pdx = signX;
            pdy = 0;
            es = dy;
            el = dx;
        }
        else {
            pdx = 0;
            pdy = signY;
            es = dx;
            el = dy;
        }
        int x = x0;
        int y = y0;
        int err = el / 2;
        g.drawLine(x, y, x, y);
        for (int i = 0; i < el; i++) {
            err -= es;
            if (err < 0) {
                err += el;
                x += signX;
                y += signY;
            }
            else {
                x += pdx;
                y += pdy;
            }
            g.drawLine(x, y, x, y);
        }
    }

}
