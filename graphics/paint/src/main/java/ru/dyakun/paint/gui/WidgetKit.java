package ru.dyakun.paint.gui;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class WidgetKit {

    private WidgetKit() {
        throw new AssertionError();
    }

    public static JRadioButton jRadioButtonFromAction(Action action, boolean hideText) {
        JRadioButton button = new JRadioButton();
        button.setIcon((Icon) action.getValue(Action.SMALL_ICON));
        button.setToolTipText((String) action.getValue(Action.SHORT_DESCRIPTION));
        if (!hideText) {
            button.setText((String) action.getValue(Action.NAME));
        }
        button.addActionListener(action);
        button.addItemListener(e -> {
            if(e.getStateChange() == ItemEvent.SELECTED) {
                button.setBackground(Colors.DARK_BACK_COLOR);
            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                button.setBackground(Colors.LIGHT_BACK_COLOR);
            }
        });
        Object selectedKey = action.getValue(Action.SELECTED_KEY);
        if(selectedKey != null) {
            if(selectedKey instanceof Boolean val) {
                button.setSelected(val);
            }
        }
        return button;
    }

}
