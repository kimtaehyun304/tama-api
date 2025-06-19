package org.example.tamaapi.repository.item.query.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;
import org.example.tamaapi.dto.UploadFile;

@Getter
//categoryItem에서 사용
@Setter
public class CategoryBestItemQueryResponse {

    @JsonIgnore
    private Long itemId;

    private Long colorItemId;

    private String name;

    private Integer price;

    private Integer discountedPrice;

    //대표 이미지
    private UploadFile uploadFile;

    private Double avgRating = 0D;

    private Long reviewCount = 0L;

    @QueryProjection
    public CategoryBestItemQueryResponse(Long itemId, Long colorItemId, String itemName, Integer price, Integer discountedPrice) {
        this.itemId = itemId;
        this.colorItemId = colorItemId;
        this.name = itemName;
        this.price = price;
        this.discountedPrice = discountedPrice;
    }



}
