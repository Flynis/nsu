package ru.nsu.dyakun.filetransfer.server;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileBuilder {
    private final Path path;
    private final OutputStream output;
    private final long size;
    private long currentSize = 0;
    public FileBuilder(Path directory, FileAttrs attrs) throws IOException {
        //System.out.println(attrs);
        this.size = attrs.length();
        StringBuilder nameBuilder = new StringBuilder(attrs.name());
        Path path = Path.of(directory.toString(), attrs.name());
        while (Files.exists(path)) {
            nameBuilder.append('0');
            path = Path.of(directory.toString(), nameBuilder.toString());
        }
        this.path = Files.createFile(path);
        output = Files.newOutputStream(path);
    }

    public boolean isBuilt() {
        return currentSize == size;
    }

    public void append(byte[] bytes, int length) throws IOException {
        if(currentSize < size) {
            output.write(bytes, 0 ,length);
            currentSize += length;
            if(currentSize > size) {
                throw new IllegalStateException("Actual size doesn't match expected one");
            }
            //System.out.printf("%s [%d]: append %d%n", path.toString(), currentSize, length);
        }
    }

    public String getName() {
        return path.toString();
    }

    public void close() throws IOException {
        output.flush();
        output.close();
    }
}
