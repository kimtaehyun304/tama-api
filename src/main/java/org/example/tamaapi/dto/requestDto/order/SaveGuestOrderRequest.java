package org.example.tamaapi.dto.requestDto.order;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SaveGuestOrderRequest {

    @NotNull
    private String paymentId;

    //--주문 고객---
    @NotNull
    private String senderNickname;

    @NotNull
    private String senderEmail;

    @NotNull
    private String senderPhone;

    //---받는 고객---
    @NotNull
    private String receiverNickname;

    @NotNull
    private String receiverPhone;

    // 우편번호
    @NotNull
    private String zipCode;

    // 도로명 주소
    @NotNull
    private String streetAddress;

    // 상세 주소
    @NotNull
    private String detailAddress;

    @NotNull
    private String deliveryMessage;

    @NotEmpty
    private List<SaveOrderItemRequest> orderItems = new ArrayList<>();
}
