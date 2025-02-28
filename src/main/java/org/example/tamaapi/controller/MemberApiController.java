package org.example.tamaapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tamaapi.cache.MyCacheType;
import org.example.tamaapi.domain.Member;
import org.example.tamaapi.domain.Order;
import org.example.tamaapi.dto.requestDto.LoginRequest;
import org.example.tamaapi.dto.requestDto.member.SignUpMemberRequest;
import org.example.tamaapi.dto.responseDto.AccessTokenResponse;
import org.example.tamaapi.dto.responseDto.SimpleResponse;
import org.example.tamaapi.dto.responseDto.member.MemberResponse;
import org.example.tamaapi.exception.MyBadRequestException;
import org.example.tamaapi.jwt.TokenProvider;
import org.example.tamaapi.repository.ColorItemRepository;
import org.example.tamaapi.repository.ItemImageRepository;
import org.example.tamaapi.repository.MemberRepository;
import org.example.tamaapi.repository.OrderRepository;
import org.example.tamaapi.service.CacheService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

import static org.example.tamaapi.jwt.TokenProvider.ACCESS_TOKEN_DURATION;

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

    @PostMapping("/api/member/new")
    public ResponseEntity<Object> signUp(@Valid @RequestBody SignUpMemberRequest request) {
        String authString = (String) cacheService.get(MyCacheType.AUTHSTRING.getName(), request.getEmail());

        if (authString == null || authString.isEmpty())
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new SimpleResponse("유효하지 않는 인증문자"));

        if(!request.getAuthString().equals(authString))
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new SimpleResponse("인증문자 불일치"));

        cacheService.evict(MyCacheType.AUTHSTRING.getName(), request.getEmail());
        String password = bCryptPasswordEncoder.encode(request.getPassword());
        memberRepository.save(Member.builder().email(request.getEmail()).password(password).build());
        return ResponseEntity.status(HttpStatus.CREATED).body(new SimpleResponse("회원가입 성공"));
    }

    @PostMapping("/api/member/login")
    public ResponseEntity<Object> login(@Valid @RequestBody LoginRequest request) {

        Member member = memberRepository.findByEmail(request.getEmail()).orElseThrow(() -> new IllegalArgumentException("가입하지 않은 이메일"));

        if(!bCryptPasswordEncoder.matches(request.getPassword(), member.getPassword()))
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new SimpleResponse("로그인 실패"));

        String accessToken = tokenProvider.generateToken(member, ACCESS_TOKEN_DURATION);
        return ResponseEntity.status(HttpStatus.OK).body(new AccessTokenResponse(accessToken));
    }

    //포트원 결제 내역에 저장할 멤버 정보
    @GetMapping("/api/member/payment-setup")
    public ResponseEntity<MemberResponse> member(Principal principal) {
        if (principal == null || !StringUtils.hasText(principal.getName()))
            throw new IllegalArgumentException("액세스 토큰이 비었습니다.");

        Long memberId = Long.parseLong(principal.getName());
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("가입하지 않은 계정"));
        return ResponseEntity.status(HttpStatus.OK).body(new MemberResponse(member));
    }

    @GetMapping("/api/member/orders")
    public ResponseEntity<MemberResponse> orders(Principal principal) {
        if (principal == null || !StringUtils.hasText(principal.getName()))
            throw new IllegalArgumentException("액세스 토큰이 비었습니다.");

        Long memberId = Long.parseLong(principal.getName());
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("가입하지 않은 계정"));
        List<Order> orders = orderRepository.findAllWithMemberAndDeliveryByMemberId(memberId);

        return ResponseEntity.status(HttpStatus.OK).body(new MemberResponse(member));
    }


}
