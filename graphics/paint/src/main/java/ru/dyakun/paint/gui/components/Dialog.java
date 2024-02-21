package ru.dyakun.paint.gui.components;

import javax.swing.*;

import static java.awt.Dialog.ModalityType.DOCUMENT_MODAL;

public class Dialog {

    protected final JDialog dialog;

    public Dialog(String title, JFrame frame) {
        dialog = new JDialog(frame, title, DOCUMENT_MODAL);
    }

    public void show() {
        dialog.setVisible(true);
    }

    protected void hide() {
        dialog.setVisible(false);
    }

}
