package ru.nsu.dyakun.filetransfer.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Server implements AutoCloseable, Runnable {
    private final static String DIRECTORY_NAME = "uploads";
    private final ServerSocket serverSocket;
    private final List<ClientHandler> clients = new ArrayList<>();
    private boolean isRunning = false;
    private final Path uploadsDirectory;

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        Path directory = Path.of(DIRECTORY_NAME);
        if(!Files.exists(directory)) {
            uploadsDirectory = Files.createDirectory(directory);
        } else {
            if(!Files.isDirectory(directory)) {
                throw new IllegalStateException(DIRECTORY_NAME + " must be directory");
            }
            uploadsDirectory = directory;
        }
        System.out.println("Directory successfully found");
    }

    @Override
    public void close() throws IOException {
        if(!serverSocket.isClosed()) {
            serverSocket.close();
        }
    }

    @Override
    public void run() {
        isRunning = true;
        System.out.println("Server started");
        while (isRunning) {
            try {
                Socket client = serverSocket.accept();
                System.out.println("Connected: " + client.getInetAddress());
                var clientHandler = new ClientHandler(client, uploadsDirectory);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            } catch (IOException e) {
                System.out.println(e.getMessage());
                stop();
            }
        }
    }

    public void stop() {
        isRunning = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Server socket close failed");
        }
        System.out.println("Server stopped");
    }
}
