package org.example.tamaapi.dto.requestDto.order;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
//로그 남기는 로직있어서 @ToString 사용
public class MemberOrderRequest {

    @NotNull
    private String paymentId;

    //받는 고객
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
    private List<OrderItemRequest> orderItems = new ArrayList<>();

}
