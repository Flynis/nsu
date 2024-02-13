package ru.dyakun.paint;

import ru.dyakun.paint.gui.MainFrame;
import ru.dyakun.paint.model.CanvasManager;
import ru.dyakun.paint.tool.ToolManager;

public class Main {

    public static void main(String[] args) {
        CanvasManager canvasManager = new CanvasManager(1000, 600);
        ToolManager toolManager = new ToolManager(canvasManager.getCanvas());
        new MainFrame(canvasManager, toolManager);
    }

}
