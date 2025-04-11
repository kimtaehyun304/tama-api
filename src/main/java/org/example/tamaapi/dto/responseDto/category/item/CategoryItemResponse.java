package org.example.tamaapi.dto.responseDto.category.item;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.example.tamaapi.domain.item.ColorItem;
import org.example.tamaapi.domain.item.Item;

import java.util.List;

@Getter
@Setter
public class CategoryItemResponse {

    @JsonIgnore
    private Long itemId;

    private String name;

    private Integer price;

    private Integer discountedPrice;

    private List<RelatedColorItemResponse> relatedColorItems;

    public CategoryItemResponse(Item item) {
        itemId = item.getId();
        name = item.getName();
        price = item.getPrice();
        discountedPrice = item.getDiscountedPrice();
    }

    public CategoryItemResponse(Item item, ColorItem colorItem) {
        itemId = item.getId();
        name = item.getName();
        price = item.getPrice();
        discountedPrice = item.getDiscountedPrice();
    }


    public CategoryItemResponse(Item item, ColorItem colorItem, Long totalStock) {
        itemId = item.getId();
        name = item.getName();
        price = item.getPrice();
        discountedPrice = item.getDiscountedPrice();
    }

}
