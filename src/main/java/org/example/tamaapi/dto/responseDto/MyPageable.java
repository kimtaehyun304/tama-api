package org.example.tamaapi.dto.responseDto;

import lombok.Getter;
import org.springframework.data.domain.Pageable;

@Getter
public class MyPageable {
    //현재 페이지
    private int page;

    //한 페이지에 들어갈 아이템 수. (몇개로 묶을건지)
    private int size;

    //계산된 페이지 수 (db row result / size)
    private long pageCount;

    //전체 상품 수
    private long rowCount;

    //spring data jpa Page 커스텀. 페이지 0부터 시작 -> 1부터 시작 변경
    public MyPageable(Pageable pageable, long totalPages, long rowCount) {
        page = pageable.getPageNumber()+1;
        size = pageable.getPageSize();
        this.pageCount = totalPages;
        this.rowCount = rowCount;
    }

    //직접 만든 페이징. 페이지 1부터 시작
    public MyPageable(int page, int size, int rowCount) {
        this.page = page;
        this.size = size;
        this.pageCount = (int) Math.ceil((double) rowCount/size);
        this.rowCount = rowCount;
    }

}
