package ru.dyakun.paint.gui.components;

import ru.dyakun.paint.model.Canvas;
import ru.dyakun.paint.model.CanvasListener;
import ru.dyakun.paint.tool.ToolManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

public class CanvasPane extends JPanel implements MouseListener, MouseMotionListener, CanvasListener {

    private final ToolManager toolManager;
    private final Canvas canvas;
    private final Dimension size;


    public CanvasPane(ToolManager toolManager, Canvas canvas) {
        this.toolManager = toolManager;
        this.canvas = canvas;
        addMouseListener(this);
        addMouseMotionListener(this);
        canvas.addCanvasListener(this);
        var image = canvas.getImage();
        size = new Dimension(image.getWidth(), image.getHeight());
    }

    public boolean isCorrectCoordinates(int x, int y) {
        BufferedImage image = canvas.getImage();
        return x >= 0 && x < image.getWidth() && y >= 0 && y < image.getHeight();
    }

    @Override
    public Dimension getPreferredSize() {
        return size;
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.drawImage(canvas.getImage(), 0, 0, null);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (isCorrectCoordinates(e.getX(), e.getY())) {
            toolManager.getCurrent().mouseClicked(e);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (isCorrectCoordinates(e.getX(), e.getY())) {
            toolManager.getCurrent().mousePressed(e);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (isCorrectCoordinates(e.getX(), e.getY())) {
            toolManager.getCurrent().mouseReleased(e);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (isCorrectCoordinates(e.getX(), e.getY())) {
            toolManager.getCurrent().mouseEntered(e);
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (isCorrectCoordinates(e.getX(), e.getY())) {
            toolManager.getCurrent().mouseExited(e);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (isCorrectCoordinates(e.getX(), e.getY())) {
            toolManager.getCurrent().mouseDragged(e);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (isCorrectCoordinates(e.getX(), e.getY())) {
            toolManager.getCurrent().mouseMoved(e);
        }
    }

    @Override
    public void onUpdate(BufferedImage image) {
        var g = getGraphics();
        g.drawImage(image, 0, 0, null);
    }

    @Override
    public void onSourceChange(BufferedImage image) {
        size.setSize(image.getWidth(), image.getHeight());
        revalidate();
    }

}
