package ru.dyakun.paint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class CanvasPane extends JPanel implements MouseListener {

    private final PainterProxy painterProxy;
    private final Canvas canvas;


    public CanvasPane(PainterProxy painterProxy, Canvas canvas) {
        this.painterProxy = painterProxy;
        this.canvas = canvas;
        addMouseListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
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

}
