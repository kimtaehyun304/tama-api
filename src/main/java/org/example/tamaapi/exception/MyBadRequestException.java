package org.example.tamaapi.exception;

public class MyBadRequestException extends RuntimeException {
    public MyBadRequestException(String message){ super(message);}
}
