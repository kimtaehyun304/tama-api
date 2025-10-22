package org.example.tamaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.domain.user.Member;
import org.example.tamaapi.domain.user.MemberAddress;
import org.example.tamaapi.domain.user.coupon.Coupon;
import org.example.tamaapi.domain.user.coupon.CouponType;
import org.example.tamaapi.domain.user.coupon.MemberCoupon;
import org.example.tamaapi.event.SignedUpEvent;
import org.example.tamaapi.exception.MyInternalServerException;
import org.example.tamaapi.repository.CouponRepository;
import org.example.tamaapi.repository.MemberAddressRepository;
import org.example.tamaapi.repository.MemberCouponRepository;
import org.example.tamaapi.repository.MemberRepository;
import org.example.tamaapi.util.ErrorMessageUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.example.tamaapi.util.ErrorMessageUtil.NOT_FOUND_ADDRESS;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberAddressRepository memberAddressRepository;
    private final CouponRepository couponRepository;
    private final MemberCouponRepository memberCouponRepository;

    //개인정보 업데이트
    public void updateMemberInformation(Long memberId, Integer height, Integer weight) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessageUtil.NOT_FOUND_MEMBER));
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

    //-------------------------------------------------------------------------------------
    public void validateIsExists(String email, String phone) {
        if (memberRepository.existsByEmail(email))
            throw new IllegalArgumentException("이미 등록된 이메일입니다");

        if (memberRepository.existsByPhone(phone))
            throw new IllegalArgumentException("이미 등록된 핸드폰 번호입니다");
    }

    public void giveWelcomeCoupon(Long memberId) {
        try {
            Coupon coupon = new Coupon(CouponType.FIXED_DISCOUNT, 10000, LocalDate.now().plusMonths(1));
            couponRepository.save(coupon);
            //서버에서 전달하는거라 검증 불필요
            Member member = new Member(memberId);
            memberCouponRepository.save(new MemberCoupon(coupon, member, false));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new MyInternalServerException("쿠폰 발급 실패");
        }
    }

    public void createCoupon(Long memberId) {
        try {
            Coupon coupon = new Coupon(CouponType.FIXED_DISCOUNT, 10000, LocalDate.now().plusMonths(1));
            couponRepository.save(coupon);
            //서버에서 전달하는거라 검증 불필요
            Member member = new Member(memberId);
            memberCouponRepository.save(new MemberCoupon(coupon, member, false));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new MyInternalServerException("쿠폰 발급 실패");
        }
    }

}
