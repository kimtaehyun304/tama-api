package org.example.tamaapi.repository.item.query.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;
import org.example.tamaapi.dto.UploadFile;

@Getter
@Setter
public class RecommendedItemQueryResponse {

    @JsonIgnore
    private Long itemId;

    private Long colorItemId;

    private String name;

    private Integer originalPrice;

    private Integer nowPrice;

    //대표 이미지
    private UploadFile uploadFile;

    @QueryProjection
    public RecommendedItemQueryResponse(Long itemId, Long colorItemId, String itemName, Integer originalPrice, Integer nowPrice) {
        this.itemId = itemId;
        this.colorItemId = colorItemId;
        this.name = itemName;
        this.originalPrice = originalPrice;
        this.nowPrice = nowPrice;
    }

}
