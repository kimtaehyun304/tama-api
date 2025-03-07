package org.example.tamaapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tamaapi.cache.MyCacheType;
import org.example.tamaapi.dto.requestDto.member.EmailRequest;
import org.example.tamaapi.dto.requestDto.member.MyTokenRequest;
import org.example.tamaapi.dto.responseDto.AccessTokenResponse;
import org.example.tamaapi.dto.responseDto.SimpleResponse;
import org.example.tamaapi.repository.item.ColorItemRepository;
import org.example.tamaapi.repository.item.ItemImageRepository;
import org.example.tamaapi.repository.MemberRepository;
import org.example.tamaapi.service.CacheService;
import org.example.tamaapi.service.EmailService;
import org.example.tamaapi.util.RandomStringGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthenticationApiController {

    private final ColorItemRepository colorItemRepository;
    private final ItemImageRepository itemImageRepository;
    private final MemberRepository memberRepository;
    private final CacheService cacheService;
    private final EmailService emailService;

    @PostMapping("/api/auth/access-token")
    public ResponseEntity<Object> accessToken(@Valid @RequestBody MyTokenRequest tokenRequest) {
        String accessToken = (String) cacheService.get(MyCacheType.TOKEN.getName(), tokenRequest.getTempToken());

        if (accessToken == null || accessToken.isEmpty())
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new SimpleResponse("일치하는 accessToken이 없습니다."));

        cacheService.evict(MyCacheType.TOKEN.getName(), tokenRequest.getTempToken());
        return ResponseEntity.status(HttpStatus.OK).body(new AccessTokenResponse(accessToken));
    }

    @PostMapping("/api/auth/email")
    public ResponseEntity<Object> email(@Valid @RequestBody EmailRequest emailRequest) {
        memberRepository.findByEmail(emailRequest.getEmail()).ifPresent(m -> {throw new IllegalArgumentException("이미 가입된 이메일입니다");});
        String authString = RandomStringGenerator.generateRandomString(6);
        cacheService.save(MyCacheType.AUTHSTRING.getName(), emailRequest.getEmail(), authString);
        emailService.sendAuthenticationEmail(emailRequest.getEmail(), authString);
        return ResponseEntity.status(HttpStatus.OK).body(new SimpleResponse("인증메일 발송 완료. 유효기간 3분"));
    }


}
