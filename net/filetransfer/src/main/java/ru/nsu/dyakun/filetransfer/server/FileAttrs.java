package ru.nsu.dyakun.filetransfer.server;

public record FileAttrs(String name, long length) {
    public FileAttrs {
        if(name.isBlank()) {
            throw new IllegalStateException("Name is blank");
        }
        if(length <= 0) {
            throw new IllegalStateException("Length must be > 0");
        }
    }
}
