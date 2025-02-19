package org.example.tamaapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tamaapi.cache.MyCacheType;
import org.example.tamaapi.domain.Member;
import org.example.tamaapi.dto.requestDto.LoginRequest;
import org.example.tamaapi.dto.requestDto.SignUpMemberRequest;
import org.example.tamaapi.dto.responseDto.AccessTokenResponse;
import org.example.tamaapi.dto.responseDto.SimpleResponse;
import org.example.tamaapi.jwt.TokenProvider;
import org.example.tamaapi.repository.ColorItemRepository;
import org.example.tamaapi.repository.ItemImageRepository;
import org.example.tamaapi.repository.MemberRepository;
import org.example.tamaapi.service.CacheService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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

}
