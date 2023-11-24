package ru.dyakun.proxy.message;

public enum RequestCommand {
    CONNECT(1),
    BIND(2),
    UDP_ASSOCIATE(3);

    private final int number;

    public int getNumber() {
        return number;
    }

    RequestCommand(int number) {
        this.number = number;
    }

    public static RequestCommand fromNumber(int number) {
        return switch (number) {
            case 1 -> CONNECT;
            case 2 -> BIND;
            case 3 -> UDP_ASSOCIATE;
            default -> throw new IllegalArgumentException("Unknown request command");
        };
    }

}
