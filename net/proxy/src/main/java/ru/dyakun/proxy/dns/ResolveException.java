package ru.dyakun.proxy.dns;

public class ResolveException extends Exception {

    public ResolveException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResolveException(String message) {
        super(message);
    }

}
