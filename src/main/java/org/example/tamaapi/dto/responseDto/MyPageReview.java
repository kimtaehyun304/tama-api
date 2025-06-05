package org.example.tamaapi.dto.responseDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.example.tamaapi.dto.requestDto.CustomPageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Getter
public class MyPageReview<T> {

    private Double avgRating;

    private final List<T> content;

    @JsonProperty("page")
    private CustomPageable myPageable;

    //spring data jpa Page 커스텀
    public MyPageReview(Double avgRating, List<T> content, Pageable pageable, long totalPages, long totalElements) {
        this.avgRating = avgRating;
        this.content = content;
        myPageable = new CustomPageable(pageable, totalPages, totalElements);
    }


}


