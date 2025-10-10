package org.example.tamaapi.dto.requestDto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
//1. @RequestBody @Valid 용도
//2. objectMapper.readValue 용도
//3. init에서 AllArgsConstructor 용도
public class SaveOrderRequest {

    private String paymentId;

    //--주문 고객---
    @NotBlank
    private String senderNickname;

    @NotBlank
    private String senderEmail;

    //---받는 고객---
    @NotBlank
    private String receiverNickname;

    @NotBlank
    private String receiverPhone;

    // 우편번호
    @NotBlank
    private String zipCode;

    @NotBlank
    private String streetAddress;

    // 상세 주소
    @NotBlank
    private String detailAddress;

    @NotBlank
    private String deliveryMessage;

    @NotNull
    private Long memberCouponId;

    private int usedPoint;

    @NotEmpty
    private List<SaveOrderItemRequest> orderItems = new ArrayList<>();
}
