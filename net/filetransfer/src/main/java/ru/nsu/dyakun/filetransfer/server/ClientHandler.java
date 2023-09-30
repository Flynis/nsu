package ru.nsu.dyakun.filetransfer.server;

import ru.nsu.dyakun.filetransfer.protocol.Message;
import ru.nsu.dyakun.filetransfer.protocol.MessageType;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;

import static ru.nsu.dyakun.filetransfer.protocol.Constants.*;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final DataInputStream in;
    private final OutputStream out;
    private final Path uploadDirectory;
    private final byte[] buffer = new byte[1400];
    private final byte[] bufferToSend = new byte[256];
    private FileBuilder builder = null;
    private boolean isRunning = false;

    public ClientHandler(Socket socket, Path uploadDirectory) throws IOException {
        this.socket = socket;
        this.in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        this.out = socket.getOutputStream();
        this.uploadDirectory = uploadDirectory;
    }

    @Override
    public void run() {
        isRunning = true;
        System.out.println("Client handler started for " + socket.getInetAddress());
        while (isRunning) {
            try {
                Message message = Message.readFrom(in, buffer);
                handle(message);
            } catch (IOException e) {
                System.out.println(e.getMessage());
                stop();
            }
        }
    }

    private void handle(Message message) throws IOException {
        switch (message.getType()) {
            case UPLOAD_REQUEST -> {
                if(message.getLength() < FILE_LEN_SIZE + 1) {
                    send(MessageType.ERROR, "Too short message");
                    return;
                }
                FileAttrs attrs = MessageUtils.readFileAttrsFrom(message);
                builder = new FileBuilder(uploadDirectory, attrs);
                send(MessageType.OK, null);
            }
            case DATA -> {
                while(!builder.isBuilt()) {
                    builder.append(message.getPayload(), message.getLength());
                }
                builder.close();
                send(MessageType.OK, null);
                stop();
            }
            default -> send(MessageType.ERROR, "Too short message");
        }
    }

    private void send(MessageType type, String message) throws IOException {
        int length;
        if(message != null) {
            length = MessageUtils.writeStringInMessage(type, message, bufferToSend);
        } else {
            length = Message.writeIn(type, null, bufferToSend);
        }
        out.write(bufferToSend, 0, length);
        System.out.printf("Client handler: send %s %d$n", type, length);
        if(type == MessageType.ERROR) {
            stop();
        }
    }

    private void stop() {
        isRunning = false;
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("Socket close failed: " + e.getMessage());
        }
        System.out.println("Client handler stop: " + socket.getInetAddress());
    }
}
