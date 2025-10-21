package org.example.tamaapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tamaapi.cache.MyCacheType;
import org.example.tamaapi.auth.CustomPrincipal;
import org.example.tamaapi.domain.user.Authority;
import org.example.tamaapi.dto.requestDto.member.EmailRequest;
import org.example.tamaapi.dto.requestDto.member.MyTokenRequest;
import org.example.tamaapi.dto.responseDto.AccessTokenResponse;
import org.example.tamaapi.dto.responseDto.IsAdminResponse;
import org.example.tamaapi.dto.responseDto.SimpleResponse;

import org.example.tamaapi.repository.MemberRepository;
import org.example.tamaapi.service.CacheService;
import org.example.tamaapi.service.EmailService;
import org.example.tamaapi.util.ErrorMessageUtil;
import org.example.tamaapi.util.RandomStringGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthenticationApiController {

    private final MemberRepository memberRepository;
    private final CacheService cacheService;
    private final EmailService emailService;

    @PostMapping("/api/auth/access-token")
    public ResponseEntity<AccessTokenResponse> accessToken(@Valid @RequestBody MyTokenRequest tokenRequest) {
        String accessToken = (String) cacheService.get(MyCacheType.SIGN_IN_TEMP_TOKEN, tokenRequest.getTempToken());
        if(!StringUtils.hasText(accessToken))
            throw new IllegalArgumentException("일치하는 accessToken이 없습니다.");

        cacheService.evict(MyCacheType.SIGN_IN_TEMP_TOKEN, tokenRequest.getTempToken());
        return ResponseEntity.status(HttpStatus.OK).body(new AccessTokenResponse(accessToken));
    }

    @PostMapping("/api/auth/email")
    public ResponseEntity<SimpleResponse> email(@Valid @RequestBody EmailRequest emailRequest) {
        memberRepository.findByEmail(emailRequest.getEmail()).ifPresent(m -> {
            throw new IllegalArgumentException("이미 가입된 이메일입니다");
        });

        String authString = RandomStringGenerator.generateRandomString(6);
        cacheService.save(MyCacheType.SIGN_UP_AUTH_STRING, emailRequest.getEmail(), authString);
        emailService.sendAuthenticationEmail(emailRequest.getEmail(), authString);
        return ResponseEntity.status(HttpStatus.OK).body(new SimpleResponse("인증메일 발송 완료. 유효기간 3분"));
    }

    @GetMapping("/api/isAdmin")
    public ResponseEntity<IsAdminResponse> isAdmin(@AuthenticationPrincipal CustomPrincipal principal) {
        Authority authority = memberRepository.findAuthorityById(principal.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessageUtil.NOT_FOUND_MEMBER));

        if (authority != Authority.ADMIN) return ResponseEntity.ok(new IsAdminResponse(false));

        return ResponseEntity.ok(new IsAdminResponse(true));
    }

}

