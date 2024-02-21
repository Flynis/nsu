package ru.dyakun.paint.gui.components;

import ru.dyakun.paint.tool.IntegerProperty;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PropertiesDialog extends Dialog {

    protected final List<PropertyEditPane> editPanes = new ArrayList<>();
    protected final JLabel errorLabel = new JLabel("Error");

    PropertiesDialog(String title, JFrame frame) {
        super(title, frame);
    }

    public PropertiesDialog(String title, String onEmpty, JFrame frame, List<IntegerProperty> properties) {
        super(title, frame);
        init(onEmpty, properties);
    }

    protected void init(String onEmpty, List<IntegerProperty> properties) {
        dialog.setMinimumSize(new Dimension(400, properties.size() * 30 + 50));
        dialog.setResizable(false);

        JPanel buttons = WidgetKit.createConfirmButtonsPane(e -> onOk(), e -> onCancel());

        if(properties.isEmpty()) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            panel.add(new JLabel(onEmpty));
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

    protected void onConfirm() {} // for override

    protected void onOk() {
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
        errorLabel.setVisible(false);
        onConfirm();
        hide();
    }

    protected void onCancel() {
        hide();
        errorLabel.setVisible(false);
    }

    protected void beforeShow() {
        for(var pane: editPanes) {
            pane.resetValue();
        }
    }

    @Override
    public void show() {
        beforeShow();
        super.show();
    }

}
