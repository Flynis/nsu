package ru.dyakun.paint.tool;

import ru.dyakun.paint.model.Canvas;
import ru.dyakun.paint.model.ColorManager;

import java.awt.event.MouseEvent;
import java.util.List;

public class FillTool extends Tool {

    public FillTool(Canvas canvas) {
        super(canvas);
    }

    @Override
    public List<IntegerProperty> getProperties() {
        return List.of();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        canvas.fill(e.getX(), e.getY(), ColorManager.getInstance().getColor());
    }

}
