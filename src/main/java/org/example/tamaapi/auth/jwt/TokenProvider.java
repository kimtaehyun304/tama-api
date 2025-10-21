package org.example.tamaapi.auth.jwt;


import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.example.tamaapi.auth.CustomPrincipal;
import org.example.tamaapi.domain.user.Member;
import org.example.tamaapi.exception.MyExpiredJwtException;
import org.example.tamaapi.exception.OrderFailException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenProvider {

    private final JwtProperties jwtProperties;

    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofDays(1);


    public String generateToken(Member member) {
        Date now = new Date();
        return makeToken(member, new Date(now.getTime() + ACCESS_TOKEN_DURATION.toMillis()));
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

        try {
            Long memberId = Long.valueOf(claims.getSubject());
            CustomPrincipal customPrincipal = new CustomPrincipal(memberId, null);
            return new UsernamePasswordAuthenticationToken(customPrincipal, token);
        } catch (Exception e){
            throw new OrderFailException("memberId 누락");
        }

    }


    private Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(jwtProperties.getSecretKey())
                .parseClaimsJws(token)
                .getBody();
    }

}
