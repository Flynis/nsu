package ru.dyakun.paint.tool;

import ru.dyakun.paint.model.Canvas;
import ru.dyakun.paint.model.ColorManager;

import java.awt.*;

public class PolygonStampTool extends StampTool {

    public PolygonStampTool(Canvas canvas) {
        super(canvas, new IntegerProperty(4, 3, 16, "Number of vertices"));
    }

    @Override
    protected void drawStamp(int x, int y, int n, int r, int theta) {
        Color c = ColorManager.getInstance().getColor();
        canvas.drawRegularPolygon(x, y, n, r, theta, c);
    }

}
