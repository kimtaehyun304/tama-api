package org.example.tamaapi.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String password;

    private String nickname;

    private Gender gender;

    private Integer height;

    private Integer weight;

    @OneToMany(mappedBy = "member")
    List<MemberAddress> memberAddresses = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    List<Order> orders = new ArrayList<>();

    @Builder
    public Member(String email, String phone, String password, String nickname, Gender gender, Integer height, Integer weight) {
        this.email = email;
        this.phone = phone;
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
