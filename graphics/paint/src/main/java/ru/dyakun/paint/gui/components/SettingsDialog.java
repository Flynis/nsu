package ru.dyakun.paint.gui.components;

import ru.dyakun.paint.tool.IntegerProperty;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SettingsDialog {

    private final JDialog dialog;
    private final List<PropertyEditPane> editPanes = new ArrayList<>();
    private final JLabel errorLabel = new JLabel("Error");

    public SettingsDialog(JFrame frame, List<IntegerProperty> properties) {
        dialog = new JDialog(frame, "Settings", Dialog.ModalityType.DOCUMENT_MODAL);
        dialog.setMinimumSize(new Dimension(400, properties.size() * 30 + 50));
        dialog.setResizable(false);

        JPanel buttons = WidgetKit.createConfirmButtonsPane(
                e -> {
                    for(var pane: editPanes) {
                        String error = pane.validateValue();
                        if(error != null) {
                            errorLabel.setText(error);
                            errorLabel.setVisible(true);
                            return;
                        }
                    }
                    for(var pane: editPanes) {
                        pane.updateValue();
                    }
                    dialog.setVisible(false);
                    errorLabel.setVisible(false);
                },
                e -> {
                    dialog.setVisible(false);
                    errorLabel.setVisible(false);
                }
        );

        if(properties.isEmpty()) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            panel.add(new JLabel("No settings"));
            dialog.add(panel);
            dialog.add(buttons, BorderLayout.SOUTH);
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            return;
        }

        JPanel gridPanel = new JPanel(new BorderLayout());
        gridPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JPanel grid = new JPanel(new GridLayout(properties.size(), 3, 5, 3));
        for(var prop: properties) {
            PropertyEditPane editPane = new PropertyEditPane(prop);
            editPanes.add(editPane);
            var label = editPane.getLabel();
            label.setHorizontalAlignment(SwingConstants.RIGHT);
            grid.add(label);
            grid.add(editPane.getField());
            grid.add(editPane.getSlider());
        }
        gridPanel.add(grid);

        errorLabel.setForeground(Colors.ERROR_COLOR);
        JPanel bottomPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPane.add(errorLabel);
        bottomPane.add(buttons);

        dialog.add(bottomPane, BorderLayout.SOUTH);
        dialog.add(gridPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        errorLabel.setVisible(false);
    }

    public void show() {
        for(var pane: editPanes) {
            pane.resetValue();
        }
        dialog.setVisible(true);
    }

}
