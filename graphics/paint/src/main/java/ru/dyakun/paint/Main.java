package ru.dyakun.paint;

import ru.dyakun.paint.gui.MainFrame;
import ru.dyakun.paint.model.ImageManager;
import ru.dyakun.paint.tool.ToolManager;

public class Main {

    public static void main(String[] args) {
        ImageManager imageManager = new ImageManager(1000, 600);
        ToolManager toolManager = new ToolManager(imageManager.getCanvas());
        new MainFrame(imageManager, toolManager);
    }

}
