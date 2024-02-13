package ru.dyakun.paint.tool;

import ru.dyakun.paint.model.Canvas;
import ru.dyakun.paint.model.ColorManager;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

public class PencilTool extends Tool {

    private int x0;
    private int y0;
    private final IntegerProperty thickness;
    private boolean mousePressed = false;

    public PencilTool(Canvas canvas) {
        super(canvas);
        thickness = new IntegerProperty(1, 1, 36, "Thickness");
    }

    @Override
    public List<IntegerProperty> getProperties() {
        return List.of(thickness);
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
            Color c = ColorManager.getInstance().getColor();
            canvas.drawLine(x0, y0, x, y, thickness.getVal(), c);
            x0 = x;
            y0 = y;
        }
    }

}
