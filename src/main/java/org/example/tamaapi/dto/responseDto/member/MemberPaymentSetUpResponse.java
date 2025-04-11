package org.example.tamaapi.dto.responseDto.member;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.tamaapi.domain.Member;

@Getter
@AllArgsConstructor
//포트원 결제 기록에 첨부할 정보.
public class MemberPaymentSetUpResponse {

    private Long id;

    private String nickname;

    private String email;

    private String phone;

    public MemberPaymentSetUpResponse(Member member) {
        this.id = member.getId();
        this.nickname = member.getNickname();
        this.email = member.getEmail();
        this.phone = member.getPhone();
    }
}
