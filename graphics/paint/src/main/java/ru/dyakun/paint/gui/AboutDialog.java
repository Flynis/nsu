package ru.dyakun.paint.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AboutDialog extends JDialog {

    public AboutDialog(JFrame frame) {
        super(frame, "About program", Dialog.ModalityType.DOCUMENT_MODAL);
        setMinimumSize(new Dimension(500, 400));
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setText("About program");

        JPanel buttons = getButtonsPane();

        add(panel);
        add(buttons, BorderLayout.SOUTH);
        pack();
    }

    private JPanel getButtonsPane() {
        JButton okButton = new JButton("Ok");
        okButton.addActionListener(e -> setVisible(false));
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(okButton);
        return buttons;
    }

}
