package ru.dyakun.paint.gui;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.List;

public class ExtensionFileFilter extends FileFilter {

    private final String description;

    private final List<String> extensions;

    public ExtensionFileFilter(String description, List<String> extensions) {
        this.description = description;
        this.extensions = extensions.stream().map(String::toLowerCase).toList();
    }

    public boolean accept(File file) {
        if (file.isDirectory()) {
            return true;
        } else {
            String path = file.getAbsolutePath().toLowerCase();
            for (var ext: extensions) {
                if ((path.endsWith(ext) && (path.charAt(path.length() - ext.length() - 1)) == '.')) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getDescription() {
        return description;
    }

}
