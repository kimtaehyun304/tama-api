package org.example.tamaapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tamaapi.cache.MyCacheType;
import org.example.tamaapi.config.CustomPrincipal;
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
import org.example.tamaapi.dto.responseDto.SimpleResponse;
import org.example.tamaapi.dto.responseDto.member.MemberAddressesResponse;
import org.example.tamaapi.dto.responseDto.member.MemberInformationResponse;
import org.example.tamaapi.dto.responseDto.member.MemberOrderSetUpResponse;
import org.example.tamaapi.jwt.TokenProvider;
import org.example.tamaapi.repository.MemberAddressRepository;
import org.example.tamaapi.repository.MemberRepository;
import org.example.tamaapi.service.CacheService;
import org.example.tamaapi.service.MemberService;
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
public class CouponApiController {

    private final MemberRepository memberRepository;
    private final CacheService cacheService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final TokenProvider tokenProvider;
    private final MemberService memberService;
    private final MemberAddressRepository memberAddressRepository;




}
