package org.example.tamaapi.dto.requestDto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import org.springframework.data.domain.Sort;

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

    //sort
    String property;

    Sort.Direction direction ;

    public MyPageRequest(int page, int size, String property, Sort.Direction direction) {
        this.page = page;
        this.size = size;

        if(property == null || property.isEmpty())
            this.property = "createdAt";
        else
            this.property = property;

        if(direction == null)
            this.direction = Sort.Direction.DESC;
        else
            this.direction = direction;

    }
}
