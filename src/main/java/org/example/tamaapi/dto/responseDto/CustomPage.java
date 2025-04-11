package org.example.tamaapi.dto.responseDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.example.tamaapi.dto.requestDto.CustomPageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Getter
//Custom Page 객체라서 "My"Page 입니다. 멤버 마이페이지랑 혼동 주의
public class CustomPage<T> {

    private final List<T> content;

    @JsonProperty("page")
    private CustomPageable myPageable;

    //spring data jpa Page 커스텀
    public CustomPage(List<T> content, Pageable pageable, long totalPages, long totalElements) {
        this.content = content;
        myPageable = new CustomPageable(pageable, totalPages, totalElements);
    }

    //직접 만든 페이징.
    public CustomPage(List<T> content, CustomPageRequest customPageRequest, int rowCount) {
        this.content = content;
        myPageable = new CustomPageable(customPageRequest.getPage(), customPageRequest.getSize(), rowCount);
    }

}


