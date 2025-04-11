package org.example.tamaapi.config;


import lombok.RequiredArgsConstructor;
import org.example.tamaapi.config.oauth2.OAuth2FailureHandler;
import org.example.tamaapi.domain.Member;
import org.example.tamaapi.exception.UnauthorizedException;
import org.example.tamaapi.jwt.TokenProvider;
import org.example.tamaapi.config.oauth2.OAuth2AuthorizationRequestBasedOnCookieRepository;
import org.example.tamaapi.config.oauth2.OAuth2SuccessHandler;
import org.example.tamaapi.config.oauth2.OAuth2UserCustomService;
import org.example.tamaapi.repository.MemberRepository;
import org.example.tamaapi.service.CacheService;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.MethodMatchers;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.method.AuthorizationManagerBeforeMethodInterceptor;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.example.tamaapi.util.ErrorMessageUtil.NOT_AUTHENTICATED;
import static org.example.tamaapi.util.ErrorMessageUtil.NOT_FOUND_MEMBER;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true) // @PreAuthorize 활성화
public class WebSecurityConfig {

    private final TokenProvider tokenProvider;
    private final OAuth2UserCustomService oAuth2UserCustomService;
    private final TokenAuthenticationFilter tokenAuthenticationFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final OAuth2AuthorizationRequestBasedOnCookieRepository oAuth2AuthorizationRequestBasedOnCookieRepository;

    private final MemberRepository memberRepository;
    private final CacheService cacheService;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationEntryPoint forbiddenEntryPoint() {
        return new Http403ForbiddenEntryPoint(); // 403 Forbidden 처리
    }

    //CORS 필터 대신 이거 사용
    @Bean
    UrlBasedCorsConfigurationSource corsConfigurationSource() {

        List<String> allowedOrigins = List.of(
                "http://localhost:3000",
                "https://dlpoma.store");

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));  // 헤더 허용
        configuration.setAllowCredentials(true);// 인증 포함 요청 허용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //권한 필요한건 @PreAuthorize
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((request) -> {
                            request.anyRequest().permitAll();
                        }
                );

        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.logout(AbstractHttpConfigurer::disable);
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 헤더를 확인할 커스텀 필터 추가.
        http.addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        http.oauth2Login(oauth2 -> {
                    oauth2
                            .authorizationEndpoint(
                                    authEndPoint ->
                                            authEndPoint.authorizationRequestRepository(
                                                    oAuth2AuthorizationRequestBasedOnCookieRepository))
                            .userInfoEndpoint(
                                    userInfoEndpointConfig ->
                                            userInfoEndpointConfig.userService(
                                                    oAuth2UserCustomService));
                    // 인증 성공 시 실행할 핸들러
                    oauth2.successHandler(oAuth2SuccessHandler);
                    oauth2.failureHandler(oAuth2FailureHandler);
                }
        );

        // 인증되지 않은 접근 시 403 오류 처리
        http.exceptionHandling((exceptionHandling) ->
                exceptionHandling.authenticationEntryPoint(forbiddenEntryPoint())
        );

        return http.build();
    }


    /*
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

     */
}