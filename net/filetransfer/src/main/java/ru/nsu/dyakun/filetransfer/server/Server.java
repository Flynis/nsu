package ru.nsu.dyakun.filetransfer.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Server implements AutoCloseable, Runnable {
    private final static int DELAY = 1; // sec
    private final static String DIRECTORY_NAME = "uploads";
    private final ServerSocket serverSocket;
    private final Timer timer;
    private final List<ClientHandler> clients = new ArrayList<>();
    private boolean isRunning = false;
    private final Path uploadsDirectory;

    public void showClientsSpeed() {
        System.out.println("Connected clients: " + clients.size());
        for(var iter = clients.iterator(); iter.hasNext();) {
            var client = iter.next();
            if(client.isStopped()) {
                if(client.isFinished()) {
                    System.out.println(client + " file successfully uploaded");
                } else {
                    System.out.println(client + " stopped: upload file failed");
                }
                iter.remove();
                continue;
            }
            long readBytes = client.getAndResetReadBytes();
            double speed = (double) readBytes / 1024 / DELAY; // kb/s
            System.out.printf("%s: %.2f kb/s%n", client, speed);
        }
    }

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
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                showClientsSpeed();
            }
        };
        timer.schedule(task, DELAY * 1000, DELAY * 1000);
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
                //System.out.println("Connected: " + client.getInetAddress());
                var clientHandler = new ClientHandler(client, uploadsDirectory);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            } catch (SocketException e) {
                break;
            } catch (IOException e) {
                System.out.println(e.getMessage());
                stop();
            }
        }
    }

    public void stop() {
        if(!isRunning) {
            return;
        }
        isRunning = false;
        timer.cancel();
        try {
            serverSocket.close();
            for(var client : clients) {
                client.stop();
            }
        } catch (IOException e) {
            System.out.println("Server socket close failed");
        }
        System.out.println("Server stopped");
    }
}
