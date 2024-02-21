package ru.dyakun.paint.gui.components;

import ru.dyakun.paint.tool.IntegerProperty;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.PlainDocument;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PropertyEditPane extends JPanel implements DocumentListener {

    private final IntegerProperty prop;
    private int current;
    private boolean updatingValue = false;
    private final JSlider slider;
    private final JTextField field;
    private final JLabel label;

    public PropertyEditPane(IntegerProperty prop) {
        this.prop = prop;

        label = new JLabel(prop.getName());

        slider = new JSlider();
        slider.setMaximum(prop.getMax());
        slider.setMinimum(prop.getMin());
        slider.setMajorTickSpacing(prop.getMax() - prop.getMin());
        slider.setPaintLabels(true);

        field = new JTextField();
        PlainDocument doc = (PlainDocument) field.getDocument();
        doc.setDocumentFilter(new IntegerFilter());
        doc.addDocumentListener(this);
        field.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!((c >= '0') && (c <= '9') ||
                        (c == KeyEvent.VK_BACK_SPACE) ||
                        (c == KeyEvent.VK_DELETE))) {
                    e.consume();
                }
            }
        });
        slider.addChangeListener(e -> {
            if (!updatingValue) {
                setValue(slider.getValue(), slider);
            }
        });

        add(label);
        add(field);
        add(slider);

        setValue(prop.getVal(), null);
    }

    private void setValue(int val, Object source) {
        current = val;
        updatingValue = true;
        if(slider != source) {
            slider.setValue(current);
        }
        if (field != source) {
            field.setText(Integer.toString(current));
        }
        updatingValue = false;
    }

    public JSlider getSlider() {
        return slider;
    }

    public JTextField getField() {
        return field;
    }

    public JLabel getLabel() {
        return label;
    }

    public void resetValue() {
        setValue(prop.getVal(), null);
        slider.setMinimum(prop.getMin());
        slider.setMaximum(prop.getMax());
        slider.setMajorTickSpacing(prop.getMax() - prop.getMin());
        System.out.println(slider.getMajorTickSpacing());
        System.out.println(prop.getMax() - prop.getMin());
    }

    public void updateValue() {
        prop.setVal(current);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        onTextFieldChanged();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        onTextFieldChanged();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        onTextFieldChanged();
    }

    private void onTextFieldChanged() {
        if (!updatingValue) {
            String text = field.getText();
            int val = text.isEmpty() ? prop.getMin() : Integer.parseInt(text);
            setValue(val, field);
        }
    }

    public String validateValue() {
        if (field.getText().isEmpty()) {
            return prop.getName() + " can not be empty";
        }
        if (current < prop.getMin()) {
            return prop.getName() + " too small";
        } else if (current > prop.getMax()) {
            return prop.getName() + " too big";
        }
        return null;
    }

}
