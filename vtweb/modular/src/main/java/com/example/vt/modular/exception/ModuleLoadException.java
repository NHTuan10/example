package com.example.vt.modular.exception;

public class ModuleLoadException extends RuntimeException {
    public ModuleLoadException(String message) {
        super(message);
    }

    public ModuleLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
