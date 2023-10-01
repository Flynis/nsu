package ru.nsu.dyakun.filetransfer.server;

import ru.nsu.dyakun.filetransfer.protocol.Message;
import ru.nsu.dyakun.filetransfer.protocol.MessageType;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
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
    private long readBytes = 0;
    private boolean isRunning = false;
    private boolean isStopped = false;

    public ClientHandler(Socket socket, Path uploadDirectory) throws IOException {
        this.socket = socket;
        this.in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        this.out = socket.getOutputStream();
        this.uploadDirectory = uploadDirectory;
    }

    @Override
    public void run() {
        isRunning = true;
        //System.out.println("Client handler started for " + socket.getInetAddress());
        while (isRunning) {
            try {
                Message message = Message.readFrom(in, buffer);
                handle(message);
            } catch (SocketException e) {
                break;
            } catch (Exception e) {
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
                if(builder.isBuilt()) {
                    return;
                }
                readBytes += message.getLength();
                builder.append(message.getPayload(), message.getLength());
                if(!builder.isBuilt()) {
                    return;
                }
                builder.close();
                send(MessageType.OK, null);
                stop();
                //System.out.printf("File %s successfully uploaded%n", builder.getName());
            }
            default -> send(MessageType.ERROR, "Too short message");
        }
    }

    public long getAndResetReadBytes() {
        long tmp = readBytes;
        readBytes = 0;
        return tmp;
    }

    public boolean isFinished() {
        return builder != null && builder.isBuilt();
    }

    public boolean isStopped() {
        return isStopped;
    }

    private void send(MessageType type, String message) throws IOException {
        int length;
        if(message != null) {
            length = MessageUtils.writeStringInMessage(type, message, bufferToSend);
        } else {
            length = Message.writeIn(type, null, bufferToSend);
        }
        out.write(bufferToSend, 0, length);
        if(type == MessageType.ERROR) {
            stop();
        }
    }

    public void stop() {
        if(!isRunning) {
            return;
        }
        isStopped = true;
        isRunning = false;
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("Socket close failed: " + e.getMessage());
        }
        //System.out.println("Client handler stop for " + socket.getInetAddress());
    }

    public String toString() {
        if(builder != null) {
            return builder.getName();
        } else {
            return socket.getInetAddress().toString();
        }
    }
}
