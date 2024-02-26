package ru.dyakun.paint.tool;

import ru.dyakun.paint.model.Canvas;

import java.awt.event.MouseEvent;
import java.util.List;

public abstract class StampTool extends Tool {

    private final IntegerProperty n;
    private final IntegerProperty radius;
    private final IntegerProperty theta;

    public void setN(int n) {
        this.n.setVal(n);
    }

    public StampTool(Canvas canvas, IntegerProperty n) {
        super(canvas);
        radius = new IntegerProperty(100, 10, 10000, "Size");
        theta = new IntegerProperty(0, 0, 360, "Rotate");
        this.n = n;
    }

    @Override
    public List<IntegerProperty> getProperties() {
        return List.of(n, radius, theta);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        drawStamp(e.getX(), e.getY(), n.getVal(), radius.getVal(), theta.getVal());
    }

    protected abstract void drawStamp(int x, int y, int n, int r, int theta);

}
