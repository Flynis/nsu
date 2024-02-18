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
        Dimension size = new Dimension(500, 400);
        setMinimumSize(size);
        setPreferredSize(size);

        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JEditorPane editor = new JEditorPane();
        editor.setEditable(false);
        editor.setContentType("text/html");
        editor.setBackground(getBackground());

        JScrollPane scrollPane = new JScrollPane(editor, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(470, 360));
        panel.add(scrollPane);

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
