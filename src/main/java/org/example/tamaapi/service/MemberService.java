package org.example.tamaapi.service;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.RequiredArgsConstructor;
import org.example.tamaapi.domain.*;
import org.example.tamaapi.domain.item.ColorItemSizeStock;
import org.example.tamaapi.dto.requestDto.order.SaveOrderItemRequest;
import org.example.tamaapi.repository.*;
import org.example.tamaapi.util.ErrorMessageUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.example.tamaapi.util.ErrorMessageUtil.NOT_FOUND_ADDRESS;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberAddressRepository memberAddressRepository;

    //개인정보 업데이트
    public void updateMemberInformation(Long memberId, Integer height, Integer weight) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException(ErrorMessageUtil.NOT_FOUND_MEMBER));
        member.changeInformation(height, weight);
    }

    public void saveMemberAddress(Long memberId, String name, String receiverNickname, String receiverPhone, String zipCode, String street, String detail) {
        if (memberAddressRepository.existsByMemberIdAndZipCodeAndStreetAndDetail(memberId, zipCode, street, detail))
            throw new IllegalArgumentException("이미 등록된 주소입니다.");

        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException(ErrorMessageUtil.NOT_FOUND_MEMBER));
        boolean isDefault = !memberAddressRepository.existsByMemberId(memberId);
        memberAddressRepository.save(new MemberAddress(name, receiverNickname, receiverPhone, zipCode, street, detail, member, isDefault));
    }

    public void updateMemberDefaultAddress(Long memberId, Long addressId) {
        //기존 배송지 default 해재 (false)
        MemberAddress defaultMemberAddress = memberAddressRepository.findByMemberIdAndIsDefault(memberId, true).orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_ADDRESS));
        defaultMemberAddress.updateIsDefault(false);

        //신규 배송지 default true
        MemberAddress memberAddress = memberAddressRepository.findById(addressId).orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_ADDRESS));
        memberAddress.updateIsDefault(true);

    }

}
