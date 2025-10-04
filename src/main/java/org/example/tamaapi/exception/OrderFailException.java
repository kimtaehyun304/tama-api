package org.example.tamaapi.exception;

public class OrderFailException extends RuntimeException {
    public OrderFailException(String message) {
        super(message);
    }
}
