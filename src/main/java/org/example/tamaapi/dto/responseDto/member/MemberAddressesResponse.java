package org.example.tamaapi.dto.responseDto.member;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.tamaapi.domain.Gender;
import org.example.tamaapi.domain.Member;
import org.example.tamaapi.domain.MemberAddress;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
//마이페이지 배송지
public class MemberAddressesResponse {

    private Long id;

    private String name;

    private String receiverNickname;

    private String receiverPhone;

    private String zipCode;

    // 도로명 주소
    private String street;

    // 상세 주소
    private String detail;

    private Boolean isDefault;

    public MemberAddressesResponse(MemberAddress memberAddress) {
        id = memberAddress.getId();
        name = memberAddress.getName();
        receiverNickname = memberAddress.getReceiverNickName();
        receiverPhone = memberAddress.getReceiverPhone();
        zipCode = memberAddress.getZipCode();
        street = memberAddress.getStreet();
        detail = memberAddress.getDetail();
        isDefault = memberAddress.isDefault();
    }
}
