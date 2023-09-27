package ru.nsu.dyakun.filetransfer.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Server implements AutoCloseable, Runnable {
    private final ServerSocket serverSocket;
    private final List<ClientHandler> clients = new ArrayList<>();
    private boolean isRunning = false;
    private final Path uploadsDirectory;

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        uploadsDirectory = Files.createDirectory(Paths.get("uploads"));
    }

    @Override
    public void close() throws IOException {
        serverSocket.close();
    }

    @Override
    public void run() {
        isRunning = true;
        while (isRunning) {
            try {
                Socket client = serverSocket.accept();
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
        System.out.println("Server stopped");
    }
}
