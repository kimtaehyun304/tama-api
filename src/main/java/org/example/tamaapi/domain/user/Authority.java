package org.example.tamaapi.domain.user;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

// implements GrantedAuthority getAuthority 어짜피 String 반환이라 제거
// getAuthority().getAuthority() 이런식으로 되는 문제도 있어서
@Getter
public enum Authority {

    //@PreAuthorize가 ROLE_000으로 비교하므로
    MEMBER("ROLE_MEMBER"),
    ADMIN("ROLE_ADMIN");

    private final String role;

    Authority(String role) {
        this.role = role;
    }
}
