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

    public SettingsDialog(JFrame frame, List<IntegerProperty> properties) {
        dialog = new JDialog(frame, "Settings", Dialog.ModalityType.DOCUMENT_MODAL);
        initByProperties(properties);
    }

    public void show() {
        dialog.setVisible(true);
    }

    private void initByProperties(List<IntegerProperty> properties) {
        dialog.setMinimumSize(new Dimension(400, 60 * properties.size()));
        dialog.setLocationRelativeTo(null);

        List<JSlider> sliders = new ArrayList<>();
        if(properties.isEmpty()) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            panel.add(new JLabel("No settings"));
            dialog.add(panel);
            dialog.add(getButtonsPane(properties, sliders), BorderLayout.SOUTH);
            dialog.pack();
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
            slider.setMajorTickSpacing(prop.getMax() - 1);
            slider.setValue(prop.getVal());
            slider.setPaintLabels(true);
            line.add(slider);
            sliders.add(slider);

            field.addActionListener(e -> slider.setValue((Integer) field.getValue()));
            slider.addChangeListener(e -> field.setValue(slider.getValue()));
            grid.add(line);
        }

        JPanel buttons = getButtonsPane(properties, sliders);

        dialog.add(grid);
        dialog.add(buttons, BorderLayout.SOUTH);
        dialog.pack();
    }

    private JPanel getButtonsPane(List<IntegerProperty> properties, List<JSlider> sliders) {
        JButton okButton = new JButton("Ok");
        okButton.addActionListener(e -> {
            for(int i = 0; i < properties.size(); i++) {
                var prop = properties.get(i);
                prop.setVal(sliders.get(i).getValue());
            }
            dialog.setVisible(false);
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            dialog.setVisible(false);
            for(int i = 0; i < properties.size(); i++) {
                var slider = sliders.get(i);
                slider.setValue(properties.get(i).getVal());
            }
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(okButton);
        buttons.add(cancelButton);
        return buttons;
    }

}
