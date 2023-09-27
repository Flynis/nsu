package ru.nsu.dyakun.filetransfer.server;

import ru.nsu.dyakun.filetransfer.protocol.Message;
import ru.nsu.dyakun.filetransfer.protocol.MessageType;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Path;

import static ru.nsu.dyakun.filetransfer.protocol.Constants.*;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final DataInputStream in;
    private final OutputStream out;
    private final Path uploadDirectory;
    private final byte[] buffer = new byte[1024 * 1024 * 15];
    private final byte[] bufferToSend = new byte[256];
    private String fileName = null;
    private long fileSize = 0;
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
        while (isRunning) {
            try {
                Message message = Message.readFrom(in, buffer);
                handle(message);
            } catch (IOException e) {
                System.out.println(e.getMessage());
                break;
            }
        }
    }

    private void handle(Message message) throws IOException {
        switch (message.getType()) {
            case UPLOAD_REQUEST -> {
                ByteBuffer byteBuffer = ByteBuffer.wrap(message.getPayload());
                if(message.getLength() < FILE_LEN_SIZE + 1) {
                    send(MessageType.ERROR, "Too short message");
                    return;
                }
                fileSize = byteBuffer.getLong();
                int fileNameLength = message.getLength() - FILE_LEN_SIZE;
                fileName = new String(byteBuffer.array(), FILE_LEN_SIZE, fileNameLength, MESSAGE_CHARSET);

                send(MessageType.OK, null);
            }
            case DATA -> {

            }
            default -> send(MessageType.ERROR, "Too short message");
        }
    }

    private void send(MessageType type, String message) throws IOException {
        int length;
        if(message != null) {
            length = Message.writeIn(type, message.getBytes(MESSAGE_CHARSET), bufferToSend);
        } else {
            length = Message.writeIn(type, null, bufferToSend);
        }
        out.write(bufferToSend, 0, length);
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
    }
}
