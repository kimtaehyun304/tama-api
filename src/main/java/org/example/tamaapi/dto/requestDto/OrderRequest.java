package org.example.tamaapi.dto.requestDto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class OrderRequest {

    //주문 고객
    private String senderNickname;
    private String senderEmail;
    private String senderPhone;

    //받는 고객
    private String receiverNickname;
    private String receiverPhone;

    // 우편번호
    private String zipCode;

    // 도로명 주소
    private String streetAddress;

    // 상세 주소
    private String detailAddress;

    private String message;

    //colorItemSizeStockId
    @NotNull
    private Long id;

    //orderCount
    @NotNull
    private Integer count;

}
