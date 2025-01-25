package org.example.tamaapi.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    Long id;

    @Column(nullable = false)
    String email;

    @Column(nullable = false)
    String password;

    String nickname;

    Gender gender;

    Integer height;

    Integer weight;

    @Builder
    public Member(String email, String password, String nickname, Gender gender, Integer height, Integer weight) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
    }

    public void changeNickname(String nickname){
        this.nickname = nickname;
    }


}
