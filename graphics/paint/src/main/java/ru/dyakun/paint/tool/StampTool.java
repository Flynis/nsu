package ru.dyakun.paint.tool;

import ru.dyakun.paint.model.Canvas;

import java.awt.event.MouseEvent;
import java.util.List;

public abstract class StampTool extends Tool {

    private int n;
    private final IntegerProperty radius;
    private final IntegerProperty theta;

    public void setN(int n) {
        this.n = n;
    }

    public StampTool(Canvas canvas) {
        super(canvas);
        radius = new IntegerProperty(5, 5, 100, "Radius");
        theta = new IntegerProperty(0, 0, 360, "Rotate");
    }

    @Override
    public List<IntegerProperty> getProperties() {
        return List.of(radius, theta);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        drawStamp(e.getX(), e.getY(), n, radius.getVal(), theta.getVal());
    }

    protected abstract void drawStamp(int x, int y, int n, int r, int theta);

}
