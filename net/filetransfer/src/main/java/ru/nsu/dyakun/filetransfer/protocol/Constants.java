package ru.nsu.dyakun.filetransfer.protocol;

public class Constants {
    private Constants() {
        throw new AssertionError();
    }

    public static int MESSAGE_TYPE_SIZE = Byte.BYTES;
    public static int MESSAGE_LEN_SIZE = Long.BYTES;
    public static int MESSAGE_MIN_LENGTH = MESSAGE_LEN_SIZE + MESSAGE_TYPE_SIZE;
}
