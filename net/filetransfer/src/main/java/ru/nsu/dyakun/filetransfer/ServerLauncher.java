package ru.nsu.dyakun.filetransfer;

import ru.nsu.dyakun.filetransfer.server.Server;
import sun.misc.Signal;

import java.io.IOException;

public class ServerLauncher {
    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Expect port");
            return;
        }
        try (var server = new Server(Integer.parseInt(args[0]))) {
            Signal.handle(new Signal("INT"), signal -> server.stop());
            server.run();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Port parse failed " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Fatal error: " + e.getMessage());
        }
    }
}