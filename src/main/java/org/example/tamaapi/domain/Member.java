package org.example.tamaapi.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tamaapi.domain.order.Order;

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

    private String phone;

    private String password;

    @Column(nullable = false)
    private String nickname;

    private Gender gender;

    private Integer height;

    private Integer weight;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Provider provider;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Authority authority;

    @OneToMany(mappedBy = "member")
    List<MemberAddress> addresses= new ArrayList<>();

    @OneToMany(mappedBy = "member")
    List<Order> orders = new ArrayList<>();

    @Builder
    public Member(String email, String phone, String password, String nickname, Gender gender, Integer height, Integer weight, Provider provider, Authority authority) {
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.nickname = nickname;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
        this.provider = provider;
        this.authority = authority;
    }


    public void changeNickname(String nickname){
        this.nickname = nickname;
    }

    //개인정보
    public void changeInformation(Integer height, Integer weight){
        this.height = height;
        this.weight = weight;
    }


}
