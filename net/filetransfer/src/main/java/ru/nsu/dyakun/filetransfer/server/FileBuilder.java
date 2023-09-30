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
        this.size = attrs.length();
        System.out.println(directory);
        Path path = Path.of(directory.toString(), attrs.name());
        if(Files.exists(path)) {
            this.path = path;
            // TODO
        } else {
            this.path = Files.createFile(path);
        }
        output = Files.newOutputStream(path);
    }

    public boolean isBuilt() {
        return currentSize == size;
    }

    public void append(byte[] bytes, int length) throws IOException {
        if(currentSize < size) {
            output.write(bytes, 0 ,length);
            currentSize += length;
            System.out.printf("%s: append %d%n", path.toString(), length);
        }
    }

    public void close() throws IOException {
        output.flush();
        output.close();
    }
}
