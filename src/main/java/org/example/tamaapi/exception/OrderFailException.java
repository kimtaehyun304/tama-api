package org.example.tamaapi.exception;

public class OrderFailException extends RuntimeException {
    public OrderFailException(String message) {
        super("결제를 취소합니다. 원인:"+message);
    }
}
