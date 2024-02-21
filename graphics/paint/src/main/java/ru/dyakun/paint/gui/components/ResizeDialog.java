package ru.dyakun.paint.gui.components;

import ru.dyakun.paint.model.ImageManager;
import ru.dyakun.paint.tool.IntegerProperty;

import javax.swing.*;
import java.util.List;

public class ResizeDialog extends PropertiesDialog {

    private final IntegerProperty width;
    private final IntegerProperty height;
    private final ImageManager manager;

    public ResizeDialog(JFrame frame, ImageManager manager) {
        super("Resize", frame);
        this.manager = manager;
        var image = manager.getCanvas().getImage();
        width = new IntegerProperty(image.getWidth(), image.getWidth(), 10000, "Width");
        height = new IntegerProperty(image.getHeight(), image.getHeight(), 10000, "Height");
        init("", List.of(width, height));
    }

    @Override
    protected void onConfirm() {
        manager.resizeImage(width.getVal(), height.getVal());
    }

    @Override
    protected void beforeShow() {
        var image = manager.getCanvas().getImage();
        width.setVal(image.getWidth());
        width.setMin(image.getWidth());
        height.setVal(image.getHeight());
        height.setMin(image.getHeight());
        super.beforeShow();
    }

}
