package org.example.tamaapi.auth;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.Collection;

@Getter
public class CustomPrincipal {
    private final Long memberId;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomPrincipal(Long userId, Collection<? extends GrantedAuthority> authorities) {
        this.memberId = userId;
        this.authorities = authorities;
    }

}
