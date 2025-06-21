package org.example.tamaapi.repository.item.query.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CategoryItemQueryDto {

    @JsonIgnore
    private Long itemId;

    private String name;

    private Integer price;

    private Integer discountedPrice;

    private List<RelatedColorItemResponse> relatedColorItems;

    @QueryProjection
    public CategoryItemQueryDto(Long itemId, String name, Integer price, Integer discountedPrice) {
        this.itemId = itemId;
        this.name = name;
        this.price = price;
        this.discountedPrice = discountedPrice;
    }


}
