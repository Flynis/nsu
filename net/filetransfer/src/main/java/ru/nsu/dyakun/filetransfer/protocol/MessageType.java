package ru.nsu.dyakun.filetransfer.protocol;

import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public enum MessageType {
    UPLOAD_REQUEST(1),
    DATA(2),
    OK(3),
    ERROR(4);

    private final int code;

    public int getCode() {
        return code;
    }

    MessageType(int code) {
        this.code = code;
    }

    private static final Map<Integer, MessageType> map = Stream.of(values()).collect(toMap(t -> t.code, t -> t));

    public static MessageType valueOf(int code) {
        MessageType type = map.get(code);
        if(type == null) {
            throw new IllegalArgumentException("Unknown code");
        }
        return type;
    }
}
