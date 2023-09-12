package ru.nsu.dyakun;

public enum MessageType {
    Report(1),
    Leave(2);

    private final int code;

    MessageType(int code) {
        this.code = code;
    }

    int getCode() {
        return code;
    }

    public static MessageType valueOf(int code) {
        for (var type: MessageType.values()) {
            if(type.getCode() == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown code");
    }
}
