package org.example.tamaapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tamaapi.cache.MyCacheType;
import org.example.tamaapi.domain.Member;
import org.example.tamaapi.domain.MemberAddress;
import org.example.tamaapi.domain.Provider;
import org.example.tamaapi.dto.requestDto.LoginRequest;
import org.example.tamaapi.dto.requestDto.member.SaveMemberAddressRequest;
import org.example.tamaapi.dto.requestDto.member.SignUpMemberRequest;
import org.example.tamaapi.dto.requestDto.member.UpdateMemberDefaultAddressRequest;
import org.example.tamaapi.dto.requestDto.member.UpdateMemberInformationRequest;
import org.example.tamaapi.dto.responseDto.AccessTokenResponse;
import org.example.tamaapi.dto.responseDto.SimpleResponse;
import org.example.tamaapi.dto.responseDto.member.MemberAddressesResponse;
import org.example.tamaapi.dto.responseDto.member.MemberInformationResponse;
import org.example.tamaapi.dto.responseDto.member.MemberPaymentSetUpResponse;
import org.example.tamaapi.jwt.TokenProvider;
import org.example.tamaapi.repository.MemberAddressRepository;
import org.example.tamaapi.repository.item.ColorItemRepository;
import org.example.tamaapi.repository.item.ItemImageRepository;
import org.example.tamaapi.repository.MemberRepository;
import org.example.tamaapi.repository.order.OrderRepository;
import org.example.tamaapi.service.CacheService;
import org.example.tamaapi.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

import static org.example.tamaapi.jwt.TokenProvider.ACCESS_TOKEN_DURATION;
import static org.example.tamaapi.util.ErrorMessageUtil.NOT_FOUND_MEMBER;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final ColorItemRepository colorItemRepository;
    private final ItemImageRepository itemImageRepository;
    private final MemberRepository memberRepository;
    private final CacheService cacheService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final TokenProvider tokenProvider;
    private final OrderRepository orderRepository;
    private final MemberService memberService;
    private final MemberAddressRepository memberAddressRepository;

    @PostMapping("/api/member/new")
    public ResponseEntity<Object> signUp(@Valid @RequestBody SignUpMemberRequest request) {
        String authString = (String) cacheService.get(MyCacheType.AUTHSTRING.getName(), request.getEmail());

        if (authString == null || authString.isEmpty())
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new SimpleResponse("유효하지 않는 인증문자"));

        if(!request.getAuthString().equals(authString))
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new SimpleResponse("인증문자 불일치"));

        cacheService.evict(MyCacheType.AUTHSTRING.getName(), request.getEmail());
        String password = bCryptPasswordEncoder.encode(request.getPassword());
        Member member = Member.builder().email(request.getEmail()).phone(request.getPhone()).nickname(request.getNickname()).password(password).provider(Provider.LOCAL).build();
        memberRepository.save(member);
        return ResponseEntity.status(HttpStatus.CREATED).body(new SimpleResponse("회원가입 성공"));
    }

    @PostMapping("/api/member/login")
    public ResponseEntity<Object> login(@Valid @RequestBody LoginRequest request) {

        Member member = memberRepository.findByEmail(request.getEmail()).orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_MEMBER));

        if(!bCryptPasswordEncoder.matches(request.getPassword(), member.getPassword()))
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new SimpleResponse("로그인 실패"));

        String accessToken = tokenProvider.generateToken(member, ACCESS_TOKEN_DURATION);
        return ResponseEntity.status(HttpStatus.OK).body(new AccessTokenResponse(accessToken));
    }

    //포트원 결제 내역에 저장할 멤버 정보
    @GetMapping("/api/member/payment-setup")
    public ResponseEntity<MemberPaymentSetUpResponse> member(Principal principal) {
        if (principal == null || !StringUtils.hasText(principal.getName()))
            throw new IllegalArgumentException("액세스 토큰이 비었습니다.");

        Long memberId = Long.parseLong(principal.getName());
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_MEMBER));
        return ResponseEntity.status(HttpStatus.OK).body(new MemberPaymentSetUpResponse(member));
    }

    //개인정보
    @GetMapping("/api/member/information")
    public ResponseEntity<MemberInformationResponse> memberInformation(Principal principal) {
        if (principal == null || !StringUtils.hasText(principal.getName()))
            throw new IllegalArgumentException("액세스 토큰이 비었습니다.");

        Long memberId = Long.parseLong(principal.getName());
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_MEMBER));

        return ResponseEntity.status(HttpStatus.OK).body(new MemberInformationResponse(member));
    }

    //개인정보
    @PutMapping("/api/member/information")
    public ResponseEntity<SimpleResponse> updateMemberInformation(Principal principal, @Valid @RequestBody UpdateMemberInformationRequest request) {
        if (principal == null || !StringUtils.hasText(principal.getName()))
            throw new IllegalArgumentException("액세스 토큰이 비었습니다.");

        Long memberId = Long.parseLong(principal.getName());
        memberService.updateMemberInformation(memberId, request.getHeight(), request.getWeight());
        return ResponseEntity.status(HttpStatus.OK).body(new SimpleResponse("개인정보 업데이트 성공"));
    }

    //마이페이지 배송지
    @GetMapping("/api/member/address")
    public List<MemberAddressesResponse> memberAddress(Principal principal) {
        if (principal == null || !StringUtils.hasText(principal.getName()))
            throw new IllegalArgumentException("액세스 토큰이 비었습니다.");

        Long memberId = Long.parseLong(principal.getName());
        List<MemberAddress> memberAddresses = memberAddressRepository.findAllByMemberId(memberId);
        return memberAddresses.stream().map(MemberAddressesResponse::new).toList();
    }

    //마이페이지 배송지
    @PostMapping("/api/member/address")
    public ResponseEntity<SimpleResponse> memberAddress(Principal principal, @Valid @RequestBody SaveMemberAddressRequest request) {
        if (principal == null || !StringUtils.hasText(principal.getName()))
            throw new IllegalArgumentException("액세스 토큰이 비었습니다.");

        Long memberId = Long.parseLong(principal.getName());


        memberService.saveMemberAddress(memberId, request.getAddressName(), request.getReceiverNickname(), request.getReceiverPhone(), request.getZipCode(), request.getStreetAddress(), request.getDetailAddress());
        return ResponseEntity.status(HttpStatus.OK).body(new SimpleResponse("배송지 저장 성공"));
    }

    //마이페이지 배송지
    @PutMapping("/api/member/address/default")
    public ResponseEntity<SimpleResponse> memberAddress(Principal principal, @Valid @RequestBody UpdateMemberDefaultAddressRequest request) {
        if (principal == null || !StringUtils.hasText(principal.getName()))
            throw new IllegalArgumentException("액세스 토큰이 비었습니다.");

        Long memberId = Long.parseLong(principal.getName());
        memberService.updateMemberDefaultAddress(memberId, request.getAddressId());
        return ResponseEntity.status(HttpStatus.OK).body(new SimpleResponse("기본 배송지 변경 성공"));
    }

}
