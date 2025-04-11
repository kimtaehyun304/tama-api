package org.example.tamaapi.exception;

public class MyExpiredJwtException extends RuntimeException {
    public MyExpiredJwtException(String message){ super(message);}
}
