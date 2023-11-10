package ru.dyakun.snake.gui.components;

import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;

public abstract class ResizableCanvas extends Canvas {
    public ResizableCanvas() {
        widthProperty().addListener(event -> redraw());
        heightProperty().addListener(event -> redraw());
    }

    public void setParent(Pane parent) {
        parent.getChildren().add(this);
        widthProperty().bind(parent.widthProperty());
        heightProperty().bind(parent.heightProperty());
    }

    protected abstract void redraw();

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public double prefWidth(double height) {
        return getWidth();
    }

    @Override
    public double prefHeight(double width) {
        return getHeight();
    }
}
