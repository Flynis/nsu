package ru.dyakun.paint.gui.components;

import ru.dyakun.paint.model.ImageManager;
import ru.dyakun.paint.tool.IntegerProperty;

import javax.swing.*;
import java.util.List;

public class NewImageDialog extends PropertiesDialog {
    private final IntegerProperty width = new IntegerProperty(1920, 100, 10000, "Width");
    private final IntegerProperty height = new IntegerProperty(1080, 100, 10000, "Height");
    private final ImageManager manager;

    public NewImageDialog(JFrame frame, ImageManager manager) {
        super("New image", frame);
        this.manager = manager;
        init("", List.of(width, height));
    }

    @Override
    protected void onConfirm() {
        manager.createEmptyImage(width.getVal(), height.getVal());
    }

}
