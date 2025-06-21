package org.example.tamaapi.jwt;


import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.example.tamaapi.config.CustomPrincipal;
import org.example.tamaapi.config.CustomUserDetails;
import org.example.tamaapi.domain.Member;
import org.example.tamaapi.exception.MyExpiredJwtException;
import org.example.tamaapi.repository.MemberRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import static org.example.tamaapi.util.ErrorMessageUtil.NOT_FOUND_MEMBER;

@Service
@RequiredArgsConstructor
public class TokenProvider {

    private final JwtProperties jwtProperties;
    private final MemberRepository memberRepository;

    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofDays(1);


    public String generateToken(Member member, Duration expiredAt) {
        Date now = new Date();
        return makeToken(member, new Date(now.getTime() + expiredAt.toMillis()));
    }

    private String makeToken(Member member, Date expiry) {
        Date now = new Date();

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .setSubject(member.getId().toString())
                .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecretKey())
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(jwtProperties.getSecretKey())
                    .parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException e) {
            throw new IllegalArgumentException("토큰이 첨부되지 않았습니다");
        } catch (ExpiredJwtException e) {
            throw new MyExpiredJwtException("토큰 유효기간이 만료되었습니다.");
        } catch (Exception e) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }
    }

    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);

        //String stringMemberId = claims.getSubject();
        Long memberId = Long.valueOf(claims.getSubject());

        //Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_MEMBER));

        //member.getAuthority().getAuthority() : ADMIN -> ROLE_ADMIN 변환
        //Set<SimpleGrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority(member.getAuthority().getAuthority()));
        //org.springframework.security.core.userdetails.User securityUser = new org.springframework.security.core.userdetails.User(stringMemberId, ", null);
        //CustomUserDetails securityUser = new CustomUserDetails(memberId, null);
        //CustomUserDetails securityUser = new CustomUserDetails(memberId, member.getEmail(), member.getPhone(), member.getPassword(), member.getNickname(), member.getGender(), member.getHeight(), member.getWeight(), member.getProvider(), member.getAuthority());

        CustomPrincipal customPrincipal = new CustomPrincipal(memberId, null);
        return new UsernamePasswordAuthenticationToken(customPrincipal, token);
    }


    private Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(jwtProperties.getSecretKey())
                .parseClaimsJws(token)
                .getBody();
    }

}
