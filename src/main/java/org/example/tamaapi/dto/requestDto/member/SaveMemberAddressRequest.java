package org.example.tamaapi.dto.requestDto.member;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SaveMemberAddressRequest {

    //ex)우리집
    @NotNull
    private String addressName;

    @NotNull
    private String receiverNickname;

    @NotNull
    private String receiverPhone;

    @NotNull
    // 우편번호
    private String zipCode;

    @NotNull
    // 도로명 주소
    private String streetAddress;

    @NotNull
    // 상세 주소
    private String detailAddress;

}
