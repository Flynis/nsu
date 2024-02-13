package ru.dyakun.paint.gui;

import ru.dyakun.paint.tool.ToolChangeListener;
import ru.dyakun.paint.tool.ToolManager;
import ru.dyakun.paint.tool.ToolType;

import javax.swing.*;
import java.util.EnumMap;
import java.util.Map;

public class SettingsManager implements ToolChangeListener {

    private SettingsDialog current;
    private final Map<ToolType, SettingsDialog> settings = new EnumMap<>(ToolType.class);

    public SettingsManager(JFrame frame, ToolManager toolManager) {
        toolManager.addToolChangedListener(this);
        for(var entry: toolManager.getTools().entrySet()) {
            SettingsDialog dialog = new SettingsDialog(frame, entry.getValue().getProperties());
            settings.put(entry.getKey(), dialog);
        }
    }

    @Override
    public void toolChanged(ToolType type) {
        current = settings.get(type);
    }

    public void showSettings() {
        current.show();
    }
}
