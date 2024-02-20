package ru.dyakun.paint.gui.components;

import ru.dyakun.paint.tool.IntegerProperty;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class ImageSizeDialog extends JDialog {

    private final IntegerProperty width = new IntegerProperty(1920, 100, 10000, "Width");
    private final IntegerProperty height = new IntegerProperty(1080, 100, 10000, "Height");
    private final List<PropertyEditPane> editPanes = new ArrayList<>();
    private final JLabel errorLabel = new JLabel("Error");

    public ImageSizeDialog(String title, JFrame frame, ActionListener listener) {
        super(frame, title, Dialog.ModalityType.DOCUMENT_MODAL);
        Dimension size = new Dimension(500, 100);
        setMinimumSize(size);
        setPreferredSize(size);
        setResizable(false);

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
                    listener.actionPerformed(new ActionEvent(this, 0, "Confirm"));
                    setVisible(false);
                    errorLabel.setVisible(false);
                },
                e -> {
                    setVisible(false);
                    errorLabel.setVisible(false);
                }
        );

        JPanel gridPanel = new JPanel(new BorderLayout());
        gridPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        List<IntegerProperty> properties = List.of(width, height);
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

        add(bottomPane, BorderLayout.SOUTH);
        add(gridPanel);
        pack();
        setLocationRelativeTo(null);
        errorLabel.setVisible(false);
    }

    public int getWidthValue() {
        return width.getVal();
    }

    public int getHeightValue() {
        return height.getVal();
    }

}
