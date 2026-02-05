package org.example.tamaapi.domain.user;

import org.springframework.security.core.GrantedAuthority;

// implements GrantedAuthority getAuthority 어짜피 String 반환이라 제거
public enum Authority {

    //@PreAuthorize가 ROLE_000으로 비교하므로
    MEMBER, ADMIN;

    public String getRole() {
        return "ROLE_" + this.name();
    }
}
