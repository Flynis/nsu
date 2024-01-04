package ru.dyakun.dif.transport;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

public class DoubleField {

    private final TextField textField;

    public DoubleField(TextField textField) {
        this.textField = textField;
        textField.addEventFilter(KeyEvent.KEY_TYPED, keyEvent -> {
            if (!".0123456789".contains(keyEvent.getCharacter())) {
                keyEvent.consume();
            }
            if(keyEvent.getCharacter().equals(".") && textField.getText().contains(".")) {
                keyEvent.consume();
            }
        });
    }

    public double getValue() {
        return Double.parseDouble(textField.getText());
    }

}
