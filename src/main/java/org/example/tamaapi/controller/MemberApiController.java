package org.example.tamaapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tamaapi.auth.CustomPrincipal;
import org.example.tamaapi.cache.MyCacheType;
import org.example.tamaapi.domain.user.coupon.MemberCoupon;
import org.example.tamaapi.domain.user.Authority;
import org.example.tamaapi.domain.user.Member;
import org.example.tamaapi.domain.user.MemberAddress;
import org.example.tamaapi.domain.user.Provider;
import org.example.tamaapi.dto.requestDto.LoginRequest;
import org.example.tamaapi.dto.requestDto.member.SaveMemberAddressRequest;
import org.example.tamaapi.dto.requestDto.member.SignUpMemberRequest;
import org.example.tamaapi.dto.requestDto.member.UpdateMemberDefaultAddressRequest;
import org.example.tamaapi.dto.requestDto.member.UpdateMemberInformationRequest;
import org.example.tamaapi.dto.responseDto.AccessTokenResponse;
import org.example.tamaapi.dto.responseDto.MemberCouponResponse;
import org.example.tamaapi.dto.responseDto.SimpleResponse;
import org.example.tamaapi.dto.responseDto.member.MemberAddressesResponse;
import org.example.tamaapi.dto.responseDto.member.MemberInformationResponse;

import org.example.tamaapi.auth.jwt.TokenProvider;
import org.example.tamaapi.dto.responseDto.member.MemberOrderSetUpResponse;
import org.example.tamaapi.event.SignedUpEvent;
import org.example.tamaapi.repository.MemberAddressRepository;

import org.example.tamaapi.repository.MemberCouponRepository;
import org.example.tamaapi.repository.MemberRepository;
import org.example.tamaapi.service.CacheService;
import org.example.tamaapi.service.MemberService;
import org.example.tamaapi.service.RedisCacheService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.example.tamaapi.util.ErrorMessageUtil.NOT_FOUND_MEMBER;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberRepository memberRepository;
    private final CacheService cacheService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final TokenProvider tokenProvider;
    private final MemberService memberService;
    private final MemberAddressRepository memberAddressRepository;
    private final MemberCouponRepository memberCouponRepository;
    private final ApplicationEventPublisher eventPublisher;

    @PostMapping("/api/member/new")
    public ResponseEntity<SimpleResponse> signUp(@Valid @RequestBody SignUpMemberRequest request) {
        memberService.validateIsExists(request.getEmail(), request.getPhone());
        String authString = (String) cacheService.get(MyCacheType.SIGN_UP_AUTH_STRING, request.getEmail());

        if (!StringUtils.hasText(authString))
            throw new IllegalArgumentException("유효하지 않는 인증문자");

        if(!request.getAuthString().equals(authString))
            throw new IllegalArgumentException("인증문자 불일치");

        cacheService.evict(MyCacheType.SIGN_UP_AUTH_STRING, request.getEmail());

        String password = bCryptPasswordEncoder.encode(request.getPassword());
        Member member = Member.builder()
                .email(request.getEmail()).phone(request.getPhone())
                .nickname(request.getNickname()).password(password)
                .provider(Provider.LOCAL).authority(Authority.MEMBER)
                .build();
        memberRepository.save(member);
        eventPublisher.publishEvent(new SignedUpEvent(member.getId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(new SimpleResponse("회원가입 성공"));
    }

    @PostMapping("/api/member/login")
    public ResponseEntity<AccessTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail()).orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_MEMBER));

        if (!bCryptPasswordEncoder.matches(request.getPassword(), member.getPassword()))
            throw new IllegalArgumentException("로그인 실패");

        String accessToken = tokenProvider.generateToken(member);
        return ResponseEntity.status(HttpStatus.OK).body(new AccessTokenResponse(accessToken));
    }

    //개인정보
    @GetMapping("/api/member/information")
    public ResponseEntity<MemberInformationResponse> memberInformation(@AuthenticationPrincipal Long memberId) {
        if (memberId == null)
            throw new IllegalArgumentException("액세스 토큰이 비었습니다.");

        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_MEMBER));
        MemberInformationResponse memberInformationResponse = new MemberInformationResponse(member);
        return ResponseEntity.status(HttpStatus.OK).body(memberInformationResponse);
    }

    //개인정보
    @PutMapping("/api/member/information")
    public ResponseEntity<SimpleResponse> updateMemberInformation(@AuthenticationPrincipal Long memberId, @Valid @RequestBody UpdateMemberInformationRequest request) {
        if (memberId == null)
            throw new IllegalArgumentException("액세스 토큰이 비었습니다.");

        memberService.updateMemberInformation(memberId, request.getHeight(), request.getWeight());
        return ResponseEntity.status(HttpStatus.OK).body(new SimpleResponse("개인정보 업데이트 성공"));
    }

    //마이페이지 배송지
    @GetMapping("/api/member/address")
    public List<MemberAddressesResponse> memberAddress(@AuthenticationPrincipal Long memberId) {
        if (memberId == null)
            throw new IllegalArgumentException("액세스 토큰이 비었습니다.");

        List<MemberAddress> memberAddresses = memberAddressRepository.findAllByMemberId(memberId);
        return memberAddresses.stream().map(MemberAddressesResponse::new).toList();
    }

    @PostMapping("/api/member/address")
    public ResponseEntity<SimpleResponse> memberAddress(@AuthenticationPrincipal Long memberId, @Valid @RequestBody SaveMemberAddressRequest request) {
        if (memberId == null)
            throw new IllegalArgumentException("액세스 토큰이 비었습니다.");

        memberService.saveMemberAddress(memberId, request.getAddressName(), request.getReceiverNickname(), request.getReceiverPhone(), request.getZipCode(), request.getStreetAddress(), request.getDetailAddress());
        return ResponseEntity.status(HttpStatus.CREATED).body(new SimpleResponse("배송지 저장 성공"));
    }

    //마이페이지 배송지
    @PutMapping("/api/member/address/default")
    public ResponseEntity<SimpleResponse> memberAddress(@AuthenticationPrincipal Long memberId, @Valid @RequestBody UpdateMemberDefaultAddressRequest request) {
        if (memberId == null)
            throw new IllegalArgumentException("액세스 토큰이 비었습니다.");

        memberService.updateMemberDefaultAddress(memberId, request.getAddressId());
        return ResponseEntity.status(HttpStatus.OK).body(new SimpleResponse("기본 배송지 변경 성공"));
    }

    //마이페이지 배송지
    @GetMapping("/api/member/coupon")
    public List<MemberCouponResponse> memberCoupon(@AuthenticationPrincipal Long memberId) {
        if (memberId == null)
            throw new IllegalArgumentException("액세스 토큰이 비었습니다.");

        List<MemberCoupon> memberCoupons = memberCouponRepository.findNotExpiredAndUnusedCouponsByMemberId(memberId);
        return memberCoupons.stream().map(MemberCouponResponse::new).toList();
    }

    //포트원 결제 내역에 저장할 멤버 정보
    @GetMapping("/api/member/orders/setup")
    public ResponseEntity<MemberOrderSetUpResponse> member(@AuthenticationPrincipal Long memberId) {
        if (memberId == null)
            throw new IllegalArgumentException("액세스 토큰이 비었습니다.");

        Member member = memberRepository.findWithAddressesById(memberId)
                .orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_MEMBER));
        return ResponseEntity.status(HttpStatus.OK).body(new MemberOrderSetUpResponse(member));
    }

}
