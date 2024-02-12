package ru.dyakun.paint.tool;

import ru.dyakun.paint.model.Canvas;
import ru.dyakun.paint.model.ColorManager;
import ru.dyakun.paint.util.IntegerProperty;

import java.awt.event.MouseEvent;
import java.util.List;

public class FillingTool extends Tool {

    public FillingTool(Canvas canvas) {
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
