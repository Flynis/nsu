package ru.nsu.dyakun.filetransfer.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final InputStream in;
    private final OutputStream out;

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        in = socket.getInputStream();
        out = socket.getOutputStream();
    }

    @Override
    public void run() {
        while (true) {
            try {
                in.read();
            } catch (IOException e) {
                System.out.println(e.getMessage());
                break;
            }
        }
    }


}
