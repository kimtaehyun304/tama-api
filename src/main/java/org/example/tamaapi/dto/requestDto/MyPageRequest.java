package org.example.tamaapi.dto.requestDto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import org.springframework.data.domain.Sort;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class MyPageRequest {

    //현재 페이지
    @Positive
    @NotNull
    int page;

    //한 페이지에 들어갈 아이템 수. (몇개로 묶을건지)
    @Positive
    @NotNull
    int size;

    //동적 정렬 안되서 못씀
    //Sort sort;

    public MyPageRequest(int page, int size) {
        this.page = page;
        this.size = size;
    }




}
