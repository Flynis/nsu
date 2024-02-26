package ru.dyakun.paint.gui;

import ru.dyakun.paint.gui.components.PropertiesDialog;
import ru.dyakun.paint.tool.ToolChangeListener;
import ru.dyakun.paint.tool.ToolManager;
import ru.dyakun.paint.tool.ToolType;

import javax.swing.*;
import java.util.EnumMap;
import java.util.Map;

public class SettingsManager implements ToolChangeListener {

    private PropertiesDialog current;
    private final Map<ToolType, PropertiesDialog> settings = new EnumMap<>(ToolType.class);

    public SettingsManager(JFrame frame, ToolManager toolManager) {
        toolManager.addToolChangedListener(this);
        for(var entry: toolManager.getTools().entrySet()) {
            var props = entry.getValue().getProperties();
            var dialog = new PropertiesDialog("Settings", "No settings", frame, props);
            settings.put(entry.getKey(), dialog);
        }
        current = settings.get(ToolType.PENCIL);
    }

    @Override
    public void toolChanged(ToolType type) {
        current = settings.get(type);
    }

    public void showSettings() {
        current.show();
    }

}
