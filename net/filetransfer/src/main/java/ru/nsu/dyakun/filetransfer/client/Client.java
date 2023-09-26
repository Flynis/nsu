package ru.nsu.dyakun.filetransfer.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

public class Client implements Runnable, AutoCloseable {
    private final Path path;
    private final InetSocketAddress serverAddress;
    private final Socket socket;

    public Client(Path path, InetAddress address, int port) {
        if(!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("File must be regular");
        }
        this.path = path;
        this.serverAddress = new InetSocketAddress(address, port);
        socket = new Socket();
    }

    @Override
    public void run() {

    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
