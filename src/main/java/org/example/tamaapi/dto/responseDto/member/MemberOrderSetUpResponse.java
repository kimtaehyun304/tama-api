package org.example.tamaapi.dto.responseDto.member;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.tamaapi.domain.Member;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
//포트원 결제 기록에 첨부할 정보. + 배송지
public class MemberOrderSetUpResponse {

    private Long id;

    private String nickname;

    private String email;

    private String phone;

    private List<MemberAddressesResponse> addresses = new ArrayList<>();

    public MemberOrderSetUpResponse(Member member) {
        this.id = member.getId();
        this.nickname = member.getNickname();
        this.email = member.getEmail();
        this.phone = member.getPhone();
        addresses.addAll(member.getAddresses().stream().map(MemberAddressesResponse::new).toList());
    }
}
