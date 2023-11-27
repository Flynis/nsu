package ru.dyakun.proxy.message;

public enum ReplyCode {
    SUCCEEDED(0),
    SOCKS_SERVER_FAILURE(1),
    HOST_UNREACHABLE(4),
    COMMAND_NOT_SUPPORTED(7),
    ADDRESS_TYPE_NOT_SUPPORTED(8);

    private final int number;

    public int getNumber() {
        return number;
    }

    ReplyCode(int number) {
        this.number = number;
    }

}
