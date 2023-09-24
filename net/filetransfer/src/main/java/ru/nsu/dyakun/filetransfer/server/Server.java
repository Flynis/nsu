package ru.nsu.dyakun.filetransfer.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server implements AutoCloseable, Runnable {
    private final ServerSocket serverSocket;
    private final List<ClientHandler> clients = new ArrayList<>();
    private boolean isRunning = false;

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
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
                var clientHandler = new ClientHandler(client);
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
