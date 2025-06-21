package org.example.tamaapi.repository.order.query.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.tamaapi.domain.order.Delivery;

@Getter
@AllArgsConstructor
public class DeliveryResponse {

    // 우편번호
    private String zipCode;

    // 도로명 주소
    private String street;

    // 상세 주소
    private String detail;

    private String message;

    private String receiverNickname;

    private String receiverPhone;

    public DeliveryResponse(Delivery delivery) {
        this.zipCode = delivery.getZipCode();
        this.street = delivery.getStreet();
        this.detail = delivery.getDetail();
        this.message = delivery.getMessage();
        this.receiverNickname = delivery.getReceiverNickname();
        this.receiverPhone = delivery.getReceiverPhone();

    }
}
