package org.example.tamaapi.repository.order.query.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.tamaapi.domain.order.Courier;
import org.example.tamaapi.domain.order.Delivery;

@Getter
@AllArgsConstructor
public class DeliveryResponse {

    private Long id;

    // 우편번호
    private String zipCode;

    // 도로명 주소
    private String street;

    // 상세 주소
    private String detail;

    private String message;

    private String receiverNickname;

    private String receiverPhone;

    private String trackingNumber;

    private Courier courier;

    public DeliveryResponse(Delivery delivery) {
        this.id = delivery.getId();
        this.zipCode = delivery.getZipCode();
        this.street = delivery.getStreet();
        this.detail = delivery.getDetail();
        this.message = delivery.getMessage();
        this.receiverNickname = delivery.getReceiverNickname();
        this.receiverPhone = delivery.getReceiverPhone();
        this.trackingNumber = delivery.getTrackingNumber();
        this.courier = delivery.getCourier();
    }

}
