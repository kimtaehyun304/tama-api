package org.example.tamaapi.exception;

public class MyInternalServerException extends RuntimeException {
    public MyInternalServerException(String message) {
        super(message);
    }
}
