package ru.dyakun.paint.gui;

import ru.dyakun.paint.tool.IntegerProperty;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class SettingsDialog {

    private final JDialog dialog;
    private final List<IntegerProperty> properties;
    private final List<JSlider> sliders;

    public SettingsDialog(JFrame frame, List<IntegerProperty> properties) {
        dialog = new JDialog(frame, "Settings", Dialog.ModalityType.DOCUMENT_MODAL);

        this.properties = properties;
        dialog.setMinimumSize(new Dimension(400, 60 * properties.size()));

        sliders = new ArrayList<>();
        if(properties.isEmpty()) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            panel.add(new JLabel("No settings"));
            dialog.add(panel);
            dialog.add(getButtonsPane(), BorderLayout.SOUTH);
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            return;
        }

        JPanel grid = new JPanel(new GridLayout(properties.size(), 1, 10, 5));
        for(var prop: properties) {
            JPanel line = new JPanel(new FlowLayout(FlowLayout.CENTER));
            line.add(new JLabel(prop.getName()));

            NumberFormatter formatter = new NumberFormatter(NumberFormat.getInstance());
            formatter.setValueClass(Integer.class);
            formatter.setMinimum(prop.getMin());
            formatter.setMaximum(prop.getMax());
            formatter.setAllowsInvalid(false);
            formatter.setCommitsOnValidEdit(true);

            JFormattedTextField field = new JFormattedTextField(formatter);
            field.setValue(prop.getVal());
            Dimension fieldSize = new Dimension(40, 30);
            field.setMinimumSize(fieldSize);
            field.setPreferredSize(fieldSize);
            line.add(field);

            JSlider slider = new JSlider();
            slider.setMaximum(prop.getMax());
            slider.setMinimum(prop.getMin());
            slider.setMajorTickSpacing(prop.getMax() - prop.getMin());
            slider.setValue(prop.getVal());
            slider.setPaintLabels(true);
            line.add(slider);
            sliders.add(slider);

            field.addActionListener(e -> slider.setValue((Integer) field.getValue()));
            slider.addChangeListener(e -> field.setValue(slider.getValue()));
            grid.add(line);
        }

        JPanel buttons = getButtonsPane();

        dialog.add(grid);
        dialog.add(buttons, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
    }

    public void show() {
        for(int i = 0; i < properties.size(); i++) {
            var slider = sliders.get(i);
            slider.setValue(properties.get(i).getVal());
        }
        dialog.setVisible(true);
    }

    private JPanel getButtonsPane() {
        JButton okButton = new JButton("Ok");
        okButton.addActionListener(e -> {
            for(int i = 0; i < properties.size(); i++) {
                var prop = properties.get(i);
                prop.setVal(sliders.get(i).getValue());
            }
            dialog.setVisible(false);
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.setVisible(false));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(okButton);
        buttons.add(cancelButton);
        return buttons;
    }

}
