package org.example.tamaapi.dto.responseDto.member;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.tamaapi.domain.Gender;
import org.example.tamaapi.domain.Member;
import org.example.tamaapi.domain.MemberAddress;
import org.example.tamaapi.domain.Provider;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
//마이페이지 개인정보 조회
public class MemberInformationResponse {

    private String email;

    private String phone;

    private String nickname;

    private Gender gender;

    private Integer height;

    private Integer weight;

    public MemberInformationResponse(Member member) {
        this.email = member.getEmail();
        this.phone = member.getPhone();
        this.nickname = member.getNickname();
        this.gender = member.getGender();
        this.height = member.getHeight();
        this.weight = member.getWeight();
    }
}
