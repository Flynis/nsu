package ru.dyakun.paint.gui.components;

import ru.dyakun.paint.tool.ToolChangeListener;
import ru.dyakun.paint.tool.ToolManager;
import ru.dyakun.paint.tool.ToolType;

import javax.swing.*;
import java.util.EnumMap;
import java.util.Map;

public class ToolRadioButtonGroup implements ToolChangeListener {

    private JRadioButton current;
    private final Map<ToolType, JRadioButton> buttons = new EnumMap<>(ToolType.class);

    public ToolRadioButtonGroup(ToolManager toolManager) {
        toolManager.addToolChangedListener(this);
    }

    public void add(ToolType type, JRadioButton radioButton) {
        current = radioButton;
        buttons.put(type, radioButton);
    }

    @Override
    public void toolChanged(ToolType type) {
        current.setSelected(false);
        current = buttons.get(type);
        current.setSelected(true);
    }

}
