package org.example.tamaapi.domain.user;

import org.springframework.security.core.GrantedAuthority;

// implements GrantedAuthority getAuthority 어짜피 String 반환이라 제거
public enum Authority  {
    MEMBER("ROLE_MEMBER"),
    ADMIN("ROLE_ADMIN");

    private final String authority;

    Authority(String authority) {
        this.authority = authority;
    }

}
