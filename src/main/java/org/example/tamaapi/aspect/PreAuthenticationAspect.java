package org.example.tamaapi.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.example.tamaapi.auth.CustomPrincipal;
import org.example.tamaapi.auth.CustomUserDetails;
import org.example.tamaapi.domain.user.Member;
import org.example.tamaapi.exception.UnauthorizedException;
import org.example.tamaapi.repository.MemberRepository;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

import static org.example.tamaapi.util.ErrorMessageUtil.NOT_AUTHENTICATED;
import static org.example.tamaapi.util.ErrorMessageUtil.NOT_FOUND_MEMBER;

@Component
@Aspect
@RequiredArgsConstructor
//@Secured("ROLE_ADMIN")보다 먼저 실행되도록 order 지정 (@Secured의 order는 100)
//order 지정하려고 config 분리
@Order(100-1)
@Slf4j
public class PreAuthenticationAspect {

    private final MemberRepository memberRepository;

    @Before("@annotation(org.example.tamaapi.aspect.PreAuthentication)")
    public void setAuthentication() {
        // 현재 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomPrincipal customPrincipal = null;
        if (authentication.getPrincipal() instanceof CustomPrincipal)
            customPrincipal = (CustomPrincipal) authentication.getPrincipal();

        //헤더에 토큰 첨부안하면 null
        if (customPrincipal == null)
            throw new UnauthorizedException(NOT_AUTHENTICATED);

        Long memberId = customPrincipal.getMemberId();
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_MEMBER));

        Set<SimpleGrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority(member.getAuthority().getAuthority()));
        Authentication newAuthentication = new UsernamePasswordAuthenticationToken(
                new CustomUserDetails(member.getId(), authorities),
                null,  // password는 필요하지 않아서 null 지정
                authorities
        );

        SecurityContextHolder.getContext().setAuthentication(newAuthentication);
    }

}