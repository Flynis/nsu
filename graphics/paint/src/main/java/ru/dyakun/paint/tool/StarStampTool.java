package ru.dyakun.paint.tool;

import ru.dyakun.paint.model.Canvas;
import ru.dyakun.paint.model.ColorManager;

import java.awt.*;

public class StarStampTool extends StampTool {

    public StarStampTool(Canvas canvas) {
        super(canvas);
    }

    @Override
    protected void drawStamp(int x, int y, int n, int r, int theta) {
        Color c = ColorManager.getInstance().getColor();
        canvas.drawRegularStar(x, y, n, r, theta, c);
    }

}
