package org.example.tamaapi.dto.responseDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.example.tamaapi.dto.requestDto.MyPageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Getter
public class MyPageReview<T> {

    private Double avgRating;

    private List<T> content;

    @JsonProperty("page")
    private MyPageable myPageable;

    //spring data jpa Page 커스텀
    public MyPageReview(Double avgRating, List<T> content, Pageable pageable, long totalPages, long totalElements) {
        this.avgRating = avgRating;
        this.content = content;
        myPageable = new MyPageable(pageable, totalPages, totalElements);
    }

    //직접 만든 페이징.
    public MyPageReview(List<T> content, MyPageRequest myPageRequest, int rowCount) {
        this.content = content;
        myPageable = new MyPageable(myPageRequest.getPage(), myPageRequest.getSize(), rowCount);
    }

}


