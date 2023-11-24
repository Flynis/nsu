package ru.dyakun.proxy.message;

public enum ReplyCode {
    SUCCEEDED(0),
    SOCKS_SERVER_FAILURE(1),
    CONNECTION_NOT_ALLOWED(2),
    NETWORK_UNREACHABLE(3),
    HOST_UNREACHABLE(4),
    CONNECTION_REFUSED(5),
    TTL_EXPIRED(6),
    COMMAND_NOT_SUPPORTED(7),
    ADDRESS_TYPE_NOT_SUPPORTED(8);

    private final int number;

    public int getNumber() {
        return number;
    }

    ReplyCode(int number) {
        this.number = number;
    }

    public static ReplyCode fromNumber(int number) {
        return switch (number) {
            case 0 -> SUCCEEDED;
            case 1 -> SOCKS_SERVER_FAILURE;
            case 2 -> CONNECTION_NOT_ALLOWED;
            case 3 -> NETWORK_UNREACHABLE;
            case 4 -> HOST_UNREACHABLE;
            case 5 -> CONNECTION_REFUSED;
            case 6 -> TTL_EXPIRED;
            case 7 -> COMMAND_NOT_SUPPORTED;
            case 8 -> ADDRESS_TYPE_NOT_SUPPORTED;
            default -> throw new IllegalArgumentException("Unknown reply code");
        };
    }

}
