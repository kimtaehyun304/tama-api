package org.example.tamaapi.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Guest {

    private String nickname;

    //private String phone;

    private String email;


    public Guest(String nickname, String email) {
        this.nickname = nickname;
        this.email = email;
    }
}
