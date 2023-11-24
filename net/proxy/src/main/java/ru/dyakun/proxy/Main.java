package ru.dyakun.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        int port = 1080;
        if(args.length > 1) {
            logger.error("Expected only <port>");
            return;
        }
        try {
            if(args.length == 1) {
                port = Integer.parseInt(args[0]);
            }
            Proxy proxy = new Proxy(port);
            proxy.listen();
        } catch (NumberFormatException e) {
            logger.error("Incorrect port: " + port);
        } catch (Exception e) {
            logger.error("Fatal error", e);
        }
    }

}