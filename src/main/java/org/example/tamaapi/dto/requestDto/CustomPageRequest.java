package org.example.tamaapi.dto.requestDto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class CustomPageRequest {

    //현재 페이지
    @Positive
    @NotNull
    int page;

    //한 페이지에 들어갈 아이템 수. (몇개로 묶을건지)
    @Positive
    @NotNull
    int size;

    //동적 정렬 안되서 못씀 + 엔티티 명 그대로 쓰는 문제
    //Sort sort;

    public CustomPageRequest(int page, int size) {
        this.page = page;
        this.size = size;
    }




}
