package org.example.tamaapi.config;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.example.tamaapi.domain.Member;
import org.example.tamaapi.dto.responseDto.SimpleResponse;
import org.example.tamaapi.exception.MyBadRequestException;
import org.example.tamaapi.repository.MemberRepository;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.security.Principal;
import java.util.Collections;
import java.util.Set;

import static org.example.tamaapi.util.ErrorMessageUtil.NOT_FOUND_MEMBER;

@Component
@Aspect
@RequiredArgsConstructor
//@Order(1)
public class AspectConfig {

    private final MemberRepository memberRepository;
    /*
    //이거말고 공통예외처리 사용
    //org.example.tamaapi.controller.* 안됨. throw new MethodArgumentNotValidException() 안됨. return ResponseEntity 안됨
    @Before("execution(* org.example.tamaapi.controller..*(.., @jakarta.validation.Valid (*), ..)))")
    public void validationAspect(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        StringBuilder message = new StringBuilder();
        for (Object arg : args) {
            if (arg instanceof BindingResult) {
                BindingResult bindingResult = (BindingResult) arg;
                if(bindingResult.hasErrors()) {
                    for (FieldError fieldError : bindingResult.getFieldErrors())
                        message.append(fieldError.getField()).append("는(은) ").append(fieldError.getDefaultMessage()).append(". ");
                    // throw BadRequestException 안돼서 예외 만듬
                    throw new MyBadRequestException(message.toString());
                }
            }
        }
    }
    */

    //@Before("execution(* org.example.tamaapi.controller..*(.., java.security.Principal, ..))")
    public void validateAccessToken(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Principal principal = null; // Principal 객체 저장

        for (Object arg : args) {
            if (arg instanceof Principal p) { // Principal 찾기
                principal = p;
                break;
            }
        }

        // Principal 검증
        if (principal == null || !StringUtils.hasText(principal.getName())) {
            throw new MyBadRequestException("액세스 토큰이 비었습니다.");
        }
    }


    @Before("@annotation(PreAuthentication)")
    public void setAuthentication() {
        // 현재 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Authentication 객체에서 CustomUserDetails 가져오기
        CustomUserDetails customUserDetails = null;
        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        }

        if (customUserDetails == null) {
            throw new IllegalArgumentException("CustomUserDetails가 존재하지 않습니다.");
        }

        Long memberId = customUserDetails.getId();

        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_MEMBER));

        Set<SimpleGrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority(member.getAuthority().getAuthority()));

        Authentication newAuthentication = new UsernamePasswordAuthenticationToken(
                new CustomUserDetails(member.getId(), authorities),
                null,  // password는 필요하지 않다면 null로 설정
                authorities
        );
        //org.springframework.security.core.userdetails.User securityUser = new org.springframework.security.core.userdetails.User(stringMemberId, ", null);

        SecurityContextHolder.getContext().setAuthentication(newAuthentication);
            /*
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new SecurityException("인증되지 않은 사용자입니다.");
            }
            */

    }

}
