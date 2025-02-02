package org.example.tamaapi.dto.responseDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Getter
public class MyPage<T> {

    private List<T> content;

    @JsonProperty("page")
    private MyPageable myPageable;

    //data jpa page 객체용
    public MyPage(List<T> content, Pageable pageable, long totalPages) {
        this.content = content;
        myPageable = new MyPageable(pageable, totalPages);
    }

    /*
    public MyPage(List<T> content, int page, int size, int rowCount) {
        this.content = content;
        myPageable = new MyPageable(page,size,rowCount);
    }*/


}


