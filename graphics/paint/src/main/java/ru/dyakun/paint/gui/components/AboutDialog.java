package ru.dyakun.paint.gui.components;

import com.github.rjeschke.txtmark.Processor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class AboutDialog extends Dialog {

    public AboutDialog(JFrame frame) {
        super("About program", frame);
        Dimension size = new Dimension(500, 400);
        dialog.setMinimumSize(size);
        dialog.setPreferredSize(size);
        dialog.setResizable(false);

        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JEditorPane editor = new JEditorPane();
        editor.setEditable(false);
        editor.setContentType("text/html");
        editor.setBackground(dialog.getBackground());

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

        JPanel buttons = WidgetKit.createConfirmButtonsPane(e -> hide());

        dialog.add(panel);
        dialog.add(buttons, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
    }

}
