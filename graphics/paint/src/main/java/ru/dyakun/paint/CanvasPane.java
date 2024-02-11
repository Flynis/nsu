package ru.dyakun.paint;

import ru.dyakun.paint.painter.ManipulatorProxy;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

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

    @Override
    public void mouseClicked(MouseEvent e) {
        manipulatorProxy.getCurrent().mouseClicked(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        manipulatorProxy.getCurrent().mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        manipulatorProxy.getCurrent().mouseReleased(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        manipulatorProxy.getCurrent().mouseEntered(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        manipulatorProxy.getCurrent().mouseExited(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        manipulatorProxy.getCurrent().mouseDragged(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        manipulatorProxy.getCurrent().mouseMoved(e);
    }

}
