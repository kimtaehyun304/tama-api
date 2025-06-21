package org.example.tamaapi.repository.item.query.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;
import org.example.tamaapi.dto.UploadFile;

@Getter
@Setter
//categoryItem에서 사용
public class RelatedColorItemResponse {

    @JsonIgnore
    private Long itemId;

    private Long colorItemId;

    private String color;

    private String hexCode;

    //대표 이미지
    private UploadFile uploadFile;

    //모든 사이즈 재고 합계
    private Integer totalStock;

    @QueryProjection
    public RelatedColorItemResponse(Long itemId, Long colorItemId,String color, String hexCode, Integer totalStock) {
        this.itemId = itemId;
        this.colorItemId = colorItemId;
        this.color = color;
        this.hexCode = hexCode;
        this.totalStock = (int) totalStock.longValue();
    }

}
