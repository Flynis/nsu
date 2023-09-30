package ru.nsu.dyakun.filetransfer.client;

import ru.nsu.dyakun.filetransfer.protocol.Message;
import ru.nsu.dyakun.filetransfer.protocol.MessageType;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

public class Client implements Runnable, AutoCloseable {
    private final Path path;
    private final InetSocketAddress serverAddress;
    private final InputStream file;
    private final Socket socket;
    private final byte[] bufferToSend = new byte[1400];
    private final byte[] buffer = new byte[256];

    public Client(Path path, InetAddress address, int port) throws IOException {
        if(!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("File must be regular");
        }
        this.file = new BufferedInputStream(Files.newInputStream(path));
        this.path = path;
        this.serverAddress = new InetSocketAddress(address, port);
        this.socket = new Socket();
        System.out.println("Desired file successfully found");
    }

    @Override
    public void run() {
        try {
            System.out.println("Client started");
            socket.connect(serverAddress);
            System.out.println("Connect to server");
            var output = socket.getOutputStream();
            var input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            int length = MessageUtils.writeUploadRequest(Files.size(path), path.getFileName().toString(), bufferToSend);
            output.write(bufferToSend, 0 , length);
            System.out.println("Send upload request " + length);
            Message response = Message.readFrom(input, buffer);
            System.out.println("Response: " + response.getType());
            if(response.getType() != MessageType.OK) {
                System.out.println("File upload failed: " + MessageUtils.readStringFrom(response));
                throw new IllegalStateException("Server sent error");
            }
            byte[] fileBuffer = new byte[1024];
            while (true) {
                int readBytes = file.read(fileBuffer);
                if(readBytes == -1) {
                    break;
                }
                length = Message.writeIn(MessageType.DATA, fileBuffer, readBytes, bufferToSend);
                output.write(bufferToSend, 0, length);
                System.out.println("Send data: " + length);
            }
            Message uploadStatus = Message.readFrom(input, buffer);
            if(uploadStatus.getType() == MessageType.OK) {
                System.out.println("File successfully uploaded");
            } else {
                System.out.println("File upload failed");
            }
        } catch (IOException | IllegalStateException e) {
            System.out.println(e.getMessage());
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void close() throws IOException {
        socket.close();
        file.close();
    }
}
