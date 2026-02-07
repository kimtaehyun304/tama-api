package org.example.tamaapi.dto.requestDto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import org.springframework.data.domain.PageRequest;

@Getter
public class CustomPageRequest {

    //int로 받으면 null 못받긴한데 에러메시지 나와서 Integer + @NotNull 사용

    //현재 페이지
    @Positive
    @NotNull
    Integer page;

    //한 페이지에 들어갈 아이템 수. (몇개로 묶을건지)
    @Positive
    @NotNull
    Integer size;

    //동적 정렬 안되서 못씀 + 엔티티 명 그대로 쓰는 문제
    //Sort sort;

    public CustomPageRequest(Integer page, Integer size) {
        this.page = page;
        this.size = size;
    }

    public PageRequest convertPageRequest(){
        return PageRequest.of(page - 1, size);
    }




}
