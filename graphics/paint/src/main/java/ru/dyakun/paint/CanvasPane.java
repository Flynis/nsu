package ru.dyakun.paint;

import ru.dyakun.paint.painter.PainterProxy;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class CanvasPane extends JPanel implements MouseListener, MouseMotionListener {

    private final PainterProxy painterProxy;
    private final Canvas canvas;


    public CanvasPane(PainterProxy painterProxy, Canvas canvas) {
        this.painterProxy = painterProxy;
        this.canvas = canvas;
        addMouseListener(this);
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
        painterProxy.getCurrent().mouseClicked(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        painterProxy.getCurrent().mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        painterProxy.getCurrent().mouseReleased(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        painterProxy.getCurrent().mouseEntered(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        painterProxy.getCurrent().mouseExited(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        painterProxy.getCurrent().mouseDragged(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        painterProxy.getCurrent().mouseMoved(e);
    }

}
