package org.example.tamaapi.dto.requestDto.order;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class SaveOrderRequest {

    private String paymentId;

    //--주문 고객---
    private String senderNickname;

    private String senderEmail;

    //---받는 고객---
    private String receiverNickname;


    private String receiverPhone;

    // 우편번호
    private String zipCode;

    // 도로명 주소
    private String streetAddress;

    // 상세 주소
    private String detailAddress;

    private String deliveryMessage;

    private List<SaveOrderItemRequest> orderItems = new ArrayList<>();
}
