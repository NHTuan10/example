package com.example.vt.web.exception;

public class ProxyCreationException extends Exception {
    public ProxyCreationException(String message) {
        super(message);
    }

    public ProxyCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
