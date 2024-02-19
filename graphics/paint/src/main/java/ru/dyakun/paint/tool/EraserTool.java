package ru.dyakun.paint.tool;

import ru.dyakun.paint.model.Canvas;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

public class EraserTool extends Tool {

    private final IntegerProperty radius;

    public EraserTool(Canvas canvas) {
        super(canvas);
        radius = new IntegerProperty(10, 1, 100, "Size");
    }

    @Override
    public List<IntegerProperty> getProperties() {
        return List.of(radius);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        erase(e.getX(), e.getY());
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        erase(e.getX(), e.getY());
    }

    private void erase(int x, int y) {
        Color c = Canvas.BACKGROUND_COLOR;
        canvas.fillCircle(x, y, radius.getVal(), c);
    }

}
