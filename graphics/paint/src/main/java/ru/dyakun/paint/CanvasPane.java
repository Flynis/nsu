package ru.dyakun.paint;

import ru.dyakun.paint.painter.ManipulatorProxy;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

public class CanvasPane extends JPanel implements MouseListener, MouseMotionListener {

    private final ManipulatorProxy manipulatorProxy;
    private final Canvas canvas;


    public CanvasPane(ManipulatorProxy manipulatorProxy, Canvas canvas) {
        this.manipulatorProxy = manipulatorProxy;
        this.canvas = canvas;
        addMouseListener(this);
        addMouseMotionListener(this);
        canvas.addChangeListener(e -> repaint());
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(Color.GRAY);
        g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
        g.drawImage(canvas.getImage(), 0, 0, null);
    }

    private boolean isInImage(int x, int y) {
        BufferedImage image = canvas.getImage();
        return x < image.getWidth() && y < image.getHeight();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (isInImage(e.getX(), e.getY())) {
            manipulatorProxy.getCurrent().mouseClicked(e);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (isInImage(e.getX(), e.getY())) {
            manipulatorProxy.getCurrent().mousePressed(e);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (isInImage(e.getX(), e.getY())) {
            manipulatorProxy.getCurrent().mouseReleased(e);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (isInImage(e.getX(), e.getY())) {
            manipulatorProxy.getCurrent().mouseEntered(e);
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (isInImage(e.getX(), e.getY())) {
            manipulatorProxy.getCurrent().mouseExited(e);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (isInImage(e.getX(), e.getY())) {
            manipulatorProxy.getCurrent().mouseDragged(e);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (isInImage(e.getX(), e.getY())) {
            manipulatorProxy.getCurrent().mouseMoved(e);
        }
    }

}
