package ru.dyakun.paint.tool;

import ru.dyakun.paint.model.Canvas;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;

public abstract class Tool implements MouseListener, MouseMotionListener {

    protected final Canvas canvas;

    public Tool(Canvas canvas) {
        this.canvas = canvas;
    }

    public void reset() {}

    public abstract List<IntegerProperty> getProperties();

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {}

    @Override
    public void mousePressed(MouseEvent mouseEvent) {}

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {}

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {}

    @Override
    public void mouseExited(MouseEvent mouseEvent) {}

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {}

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {}

}
