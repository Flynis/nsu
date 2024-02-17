package ru.dyakun.paint.gui;

import com.github.rjeschke.txtmark.Processor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class AboutDialog extends JDialog {

    public AboutDialog(JFrame frame) {
        super(frame, "About program", Dialog.ModalityType.DOCUMENT_MODAL);
        setMinimumSize(new Dimension(500, 400));

        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JEditorPane editor = new JEditorPane();
        editor.setEditable(false);
        editor.setContentType("text/html");
        editor.setBackground(getBackground());
        panel.add(editor);

        String filename = "/about.md";
        try (var inputStream = AboutDialog.class.getResourceAsStream(filename)) {
            String html = Processor.process(Objects.requireNonNull(inputStream));
            editor.setText(html);
        } catch (IOException e) {
            System.out.println("Failed to load " + filename);
            editor.setText("<h1>Paint</h1>");
        }

        add(panel);
        add(getButtonsPane(), BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel getButtonsPane() {
        JButton okButton = new JButton("Ok");
        okButton.addActionListener(e -> setVisible(false));
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(okButton);
        return buttons;
    }

}
