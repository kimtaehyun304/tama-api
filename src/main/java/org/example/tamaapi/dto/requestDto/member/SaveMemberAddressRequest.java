package org.example.tamaapi.dto.requestDto.member;

import jakarta.validation.constraints.NotBlank;
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
    @NotBlank
    private String addressName;

    @NotBlank
    private String receiverNickname;

    @NotBlank
    private String receiverPhone;

    @NotBlank
    // 우편번호
    private String zipCode;

    @NotBlank
    // 도로명 주소
    private String streetAddress;

    @NotBlank
    // 상세 주소
    private String detailAddress;

}
