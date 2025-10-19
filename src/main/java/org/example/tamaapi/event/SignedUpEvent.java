package org.example.tamaapi.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SignedUpEvent {
    private Long memberId;

    //도메인 종속성을 분리해야하므로, memberId 외 필드는 넣지 마세요
}
