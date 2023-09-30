package ru.nsu.dyakun.filetransfer;

import ru.nsu.dyakun.filetransfer.client.Client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;

public class ClientLauncher {
    public static void main(String[] args) {
        if(args.length != 3) {
            System.out.println("Expect ip, port and file name");
            return;
        }
        try (var client = new Client(Path.of(args[2]), InetAddress.getByName(args[0]), Integer.parseInt(args[1]))) {
            client.run();
        } catch (UnknownHostException e) {
            System.out.println("Unknown host" + e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Port parse failed: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Fatal error" + e.getMessage());
        }
    }
}
