package org.example.tamaapi.exception;

import lombok.Getter;

@Getter
public class OrderFailException extends RuntimeException {

    public OrderFailException(String message, String paymentId) {
        super(String.format("결제가 취소될 예정입니다. 원인:%s, 결제 번호:%s", message, paymentId));
    }

    public OrderFailException(String message) {
        super(String.format("주문이 취소됩니다. 원인:%s", message));
    }
}
