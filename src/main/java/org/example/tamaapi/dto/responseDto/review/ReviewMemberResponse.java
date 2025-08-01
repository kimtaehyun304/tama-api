package org.example.tamaapi.dto.responseDto.review;

import lombok.Getter;
import org.example.tamaapi.domain.user.Member;

@Getter
public class ReviewMemberResponse {
    private final String nickname;
    private final Integer height;
    private final Integer weight;

    public ReviewMemberResponse(Member member) {
        this.nickname = member.getNickname();
        this.height = member.getHeight();
        this.weight = member.getWeight();
    }
}
