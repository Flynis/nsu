package ru.dyakun.paint.tool;

@FunctionalInterface
public interface ToolChangeListener {

    void toolChanged(ToolType type);

}
