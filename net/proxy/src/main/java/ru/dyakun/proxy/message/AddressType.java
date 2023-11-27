package ru.dyakun.proxy.message;


public enum AddressType {
    IPV4(1),
    DOMAIN_NAME(3),
    IPV6(4);

    private final int number;

    public int getNumber() {
        return number;
    }

    AddressType(int number) {
        this.number = number;
    }

    public static AddressType fromNumber(int number) {
        return switch (number) {
            case 1 -> IPV4;
            case 3 -> DOMAIN_NAME;
            case 4 -> IPV6;
            default -> throw new IllegalArgumentException("Unknown request command");
        };
    }

}
