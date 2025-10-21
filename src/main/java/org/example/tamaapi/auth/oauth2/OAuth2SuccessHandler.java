package org.example.tamaapi.auth.oauth2;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.tamaapi.cache.MyCacheType;
import org.example.tamaapi.auth.jwt.TokenProvider;
import org.example.tamaapi.domain.user.Member;
import org.example.tamaapi.repository.MemberRepository;
import org.example.tamaapi.service.CacheService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.UUID;

import static org.example.tamaapi.util.ErrorMessageUtil.NOT_FOUND_MEMBER;


@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;
    private final OAuth2AuthorizationRequestBasedOnCookieRepository oAuth2AuthorizationRequestBasedOnCookieRepository;
    private final CacheService cacheService;

    @Value("${frontend.url}")
    private String FRONTEND_URL;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Member member = memberRepository.findByEmail((String) oAuth2User.getAttributes().get("email")).orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_MEMBER));

        String tempToken = UUID.randomUUID().toString();
        String accessToken = tokenProvider.generateToken(member);
        cacheService.save(MyCacheType.SIGN_IN_TEMP_TOKEN, tempToken, accessToken);

        String targetUrl = getTargetUrl(tempToken);

        // 인증관련 설정값, 쿠키 제거
        clearAuthenticationAttributes(request, response);
        // 리다이렉트
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String getTargetUrl(String accessToken) {
        return UriComponentsBuilder.fromUriString(FRONTEND_URL)
                .queryParam("tempToken", accessToken)
                .build()
                .toUriString();
    }


    // 인증 관련 설정값, 쿠키 제거
    private void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        oAuth2AuthorizationRequestBasedOnCookieRepository.removeAuthorizationRequestCookies(request, response);
    }


}