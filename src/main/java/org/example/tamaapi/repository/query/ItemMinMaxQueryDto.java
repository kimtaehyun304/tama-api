package org.example.tamaapi.repository.query;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.example.tamaapi.domain.ColorItem;

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
