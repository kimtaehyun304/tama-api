package org.example.tamaapi.config;



import lombok.RequiredArgsConstructor;
import org.example.tamaapi.jwt.TokenProvider;
import org.example.tamaapi.config.oauth2.OAuth2AuthorizationRequestBasedOnCookieRepository;
import org.example.tamaapi.config.oauth2.OAuth2SuccessHandler;
import org.example.tamaapi.config.oauth2.OAuth2UserCustomService;
import org.example.tamaapi.repository.MemberRepository;
import org.example.tamaapi.service.CacheService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class WebSecurityConfig {

    private final TokenProvider tokenProvider;
    private final OAuth2UserCustomService oAuth2UserCustomService;
    private final MemberRepository memberRepository;
    private final CacheService cacheService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((request) -> {
                    request.anyRequest().permitAll();
                });

        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.logout(AbstractHttpConfigurer::disable);
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 헤더를 확인할 커스텀 필터 추가
        http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        http.oauth2Login(oauth2 -> {
                    oauth2
                            .authorizationEndpoint(
                                    authEndPoint ->
                                            authEndPoint.authorizationRequestRepository(
                                                    oAuth2AuthorizationRequestBasedOnCookieRepository()))
                            .userInfoEndpoint(
                                    userInfoEndpointConfig ->
                                            userInfoEndpointConfig.userService(
                                                    oAuth2UserCustomService));
                    // 인증 성공 시 실행할 핸들러
                    oauth2.successHandler(oAuth2SuccessHandler());
                }
        );

        return http.build();
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenProvider);
    }


    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public OAuth2SuccessHandler oAuth2SuccessHandler() {
        return new OAuth2SuccessHandler(tokenProvider, memberRepository, oAuth2AuthorizationRequestBasedOnCookieRepository(), cacheService);
    }

    @Bean
    public OAuth2AuthorizationRequestBasedOnCookieRepository oAuth2AuthorizationRequestBasedOnCookieRepository() {
        return new OAuth2AuthorizationRequestBasedOnCookieRepository();
    }
}