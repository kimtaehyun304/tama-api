package org.example.tamaapi.dto.responseDto.member;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.tamaapi.domain.Member;

@Getter
@AllArgsConstructor
public class MemberOrderResponse {

    private Long id;

    private String nickname;

    private String email;

    private String phone;

    public MemberOrderResponse(Member member) {
        this.id = member.getId();
        this.nickname = member.getNickname();
        this.email = member.getEmail();
        this.phone = member.getPhone();
    }
}
