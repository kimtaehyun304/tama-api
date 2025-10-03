package org.example.tamaapi.dto.responseDto.member;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.tamaapi.domain.user.Member;

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

    private int point;

    private List<MemberAddressesResponse> addresses = new ArrayList<>();

    public MemberOrderSetUpResponse(Member member) {
        id = member.getId();
        nickname = member.getNickname();
        email = member.getEmail();
        phone = member.getPhone();
        point = member.getPoint();
        addresses.addAll(member.getAddresses().stream().map(MemberAddressesResponse::new).toList());
    }
}
