package ru.dyakun.proxy.message;

public class UnsignedNumbers {
    public static final int UNSIGNED_BYTE_MAX = 255;
    public static final int UNSIGNED_SHORT_MAX = 65535;

    private UnsignedNumbers() {
        throw new AssertionError();
    }

    public static boolean isUnsignedByte(int b) {
        return b >= 0 && b <= UNSIGNED_BYTE_MAX;
    }

    public static boolean isUnsignedShort(int s) {
        return s >= 0 && s <= UNSIGNED_SHORT_MAX;
    }

    public static int getUnsignedByte(byte b) {
        return (b < 0) ? b + UNSIGNED_BYTE_MAX + 1 : b;
    }

    public static int getUnsignedShort(short s) {
        return (s < 0) ? s + UNSIGNED_SHORT_MAX + 1 : s;
    }

}
