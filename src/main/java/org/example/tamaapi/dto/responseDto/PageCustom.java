package org.example.tamaapi.dto.responseDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Getter
public class PageCustom<T> {

    private List<T> content;

    private PageableCustom page;

    public PageCustom(List<T> content, Pageable pageable, long totalPages) {
        this.content = content;
        page = new PageableCustom(pageable, totalPages);
    }

    @Getter
    private class PageableCustom {
        //현재 페이지
        private int page;

        //한 페이지에 들어갈 아이템 수. (몇개로 묶을건지)
        private int size;

        //계산된 페이지 수 (db row result / size)
        private long pageCount;

        //Page 객체는 0부터 시작 -> 1부터 시작하게 변경
        public PageableCustom(Pageable pageable, long totalPages) {
            page = pageable.getPageNumber()+1;
            size = pageable.getPageSize();
            this.pageCount = totalPages;
        }
    }
}


