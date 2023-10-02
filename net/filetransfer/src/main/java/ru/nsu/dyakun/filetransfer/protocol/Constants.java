package ru.nsu.dyakun.filetransfer.protocol;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Constants {
    private Constants() {
        throw new AssertionError();
    }

    public static int MESSAGE_TYPE_SIZE = Byte.BYTES;
    public static Charset MESSAGE_CHARSET = StandardCharsets.UTF_8;
    public static int MESSAGE_LEN_SIZE = Integer.BYTES;
    public static int FILE_LEN_SIZE = Long.BYTES;
    public static int MESSAGE_HEADER_SIZE = MESSAGE_LEN_SIZE + MESSAGE_TYPE_SIZE;
}
