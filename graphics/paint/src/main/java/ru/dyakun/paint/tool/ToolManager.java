package ru.dyakun.paint.tool;

import ru.dyakun.paint.model.Canvas;

import java.util.*;

public class ToolManager {

    private Tool current;
    private final Map<ToolType, Tool> tools = new EnumMap<>(ToolType.class);
    private final List<ToolChangeListener> listeners = new ArrayList<>();

    public ToolManager(Canvas canvas) {
        tools.put(ToolType.LINE, new LineTool(canvas));
        tools.put(ToolType.POLYGON_STAMP, new PolygonStampTool(canvas));
        tools.put(ToolType.STAR_STAMP, new StarStampTool(canvas));
        tools.put(ToolType.FILL, new FillTool(canvas));
        tools.put(ToolType.PENCIL, new PencilTool(canvas));
        tools.put(ToolType.ERASER, new EraserTool(canvas));
        setCurrent(ToolType.PENCIL);
    }

    public void setCurrent(ToolType toolType) {
        this.current = tools.get(toolType);
        current.reset();
        System.out.println("Tool changed to " + toolType.name());
        for(var listener: listeners) {
            listener.toolChanged(toolType);
        }
    }

    public Tool getCurrent() {
        return current;
    }

    public Map<ToolType, Tool> getTools() {
        return tools;
    }

    public void addToolChangedListener(ToolChangeListener listener) {
        listeners.add(listener);
    }

}
