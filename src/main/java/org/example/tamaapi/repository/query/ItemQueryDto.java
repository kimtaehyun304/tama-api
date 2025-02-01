package org.example.tamaapi.repository.query;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.example.tamaapi.domain.ColorItem;
import org.example.tamaapi.domain.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
//categoryItem에서 사용
public class ItemQueryDto {

    @JsonIgnore
    private Long itemId;

    private String name;

    private Integer price;

    private Integer discountedPrice;

    private List<RelatedColorItemQueryDto> relatedColorItems = new ArrayList<>();

    public ItemQueryDto(Item item, Long totalStock) {
        itemId = item.getId();
        name = item.getName();
        price = item.getPrice();
        discountedPrice = item.getDiscountedPrice();
    }


    public ItemQueryDto(Item item, ColorItem colorItem, Long totalStock) {
        itemId = item.getId();
        name = item.getName();
        price = item.getPrice();
        discountedPrice = item.getDiscountedPrice();
        relatedColorItems.add(new RelatedColorItemQueryDto(colorItem, totalStock));
    }



}
