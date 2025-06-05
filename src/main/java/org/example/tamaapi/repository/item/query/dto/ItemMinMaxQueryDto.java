package org.example.tamaapi.repository.item.query.dto;

import lombok.Getter;

@Getter
//categoryItem에서 사용
public class ItemMinMaxQueryDto {

    Integer minPrice;
    Integer maxPrice;

    public ItemMinMaxQueryDto(Integer minPrice, Integer maxPrice) {
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }

}
