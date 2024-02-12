package ru.dyakun.paint.painter;

import ru.dyakun.paint.Canvas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class PolygonStampManipulator extends StampManipulator {

    public PolygonStampManipulator(Canvas canvas, JFrame frame) {
        super(canvas, frame);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int centerX = e.getX();
        int centerY = e.getY();
        Graphics2D g = (Graphics2D) canvas.getImage().getGraphics();
        g.setStroke(new BasicStroke(1));
        g.setColor(canvas.getColor());
        // calc first point coordinates
        double x1 = centerX;
        double y1 = centerY - radius.getVal();
        int a0 = theta.getVal();
        int a = (n - 2) * 180 / n;
        if(n % 2 == 0) {
            a0 += a / 2;
        }
        double sinA0 = Math.sin(a0);
        double cosA0 = Math.cos(a0);
        double x0 = cosA0 * x1 - sinA0 * y1;
        double y0 = y1 = sinA0 * x1 + cosA0 * y1;
        // calc rotate matrix
        double sinA = Math.sin(a);
        double cosA = Math.cos(a);
        double xp = x0;
        double yp = y0;
        int x = 0;
        int y = 0;
        for(int i = 1; i < n; i++) {
            x = (int) (cosA * xp - sinA * yp);
            y = (int) (sinA * xp + cosA * yp);
            g.drawLine((int) xp, (int) yp, x, y);
        }
        g.drawLine(x, y, (int) x0, (int) y0);
        canvas.repaint();
    }

}
