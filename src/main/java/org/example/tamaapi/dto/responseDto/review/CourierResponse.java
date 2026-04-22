package org.example.tamaapi.dto.responseDto.review;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CourierResponse {
    private String code;
    private String kor;
    private String eng; // enum 이름
}
