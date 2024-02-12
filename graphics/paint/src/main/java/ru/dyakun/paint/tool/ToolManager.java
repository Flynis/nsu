package ru.dyakun.paint.tool;

import ru.dyakun.paint.model.Canvas;

import java.util.EnumMap;
import java.util.Map;

public class ToolManager {

    private Tool current;
    private final Map<ToolType, Tool> tools = new EnumMap<>(ToolType.class);

    public ToolManager(Canvas canvas) {
        tools.put(ToolType.LINE, new LineTool(canvas));
        tools.put(ToolType.POLYGON_STAMP, new PolygonStampTool(canvas));
        tools.put(ToolType.STAR_STAMP, new StarStampTool(canvas));
        tools.put(ToolType.FILLING, new FillingTool(canvas));
        tools.put(ToolType.PENCIL, new PencilTool(canvas));
        tools.put(ToolType.ERASER, new EraserTool(canvas));
        setCurrent(ToolType.PENCIL);
    }

    public void setCurrent(ToolType toolType) {
        this.current = tools.get(toolType);
        current.reset();
        System.out.println("Tool changed to " + toolType.name());
    }

    public Tool getCurrent() {
        return current;
    }

}
