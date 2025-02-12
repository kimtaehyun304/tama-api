package org.example.tamaapi.dto.responseDto.review;

import lombok.Getter;
import org.example.tamaapi.domain.Member;

@Getter
public class ReviewMemberResponse {
    private String nickname;
    private Integer height;
    private Integer weight;

    public ReviewMemberResponse(Member member) {
        this.nickname = member.getNickname();
        this.height = member.getHeight();
        this.weight = member.getWeight();
    }
}
