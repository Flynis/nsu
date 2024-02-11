package ru.dyakun.paint.painter;

import ru.dyakun.paint.Canvas;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;

public class PencilManipulator extends Manipulator {

    private int x0;
    private int y0;
    private int strokeWidth = 1;
    private boolean mousePressed = false;

    public PencilManipulator(Canvas canvas, JFrame frame) {
        super(canvas, frame);
        initSettingDialog();
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
            g.setStroke(new BasicStroke(strokeWidth));
            g.setColor(canvas.getColor());
            g.drawLine(x0, y0, x, y);
            x0 = x;
            y0 = y;
            canvas.repaint();
        }
    }

    private void initSettingDialog() {

    }

}
