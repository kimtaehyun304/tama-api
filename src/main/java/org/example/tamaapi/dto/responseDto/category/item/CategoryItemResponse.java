package org.example.tamaapi.dto.responseDto.category.item;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.example.tamaapi.domain.*;
import org.example.tamaapi.repository.query.ItemMinMaxQueryDto;
import org.example.tamaapi.repository.query.RelatedColorItemQueryDto;

import java.util.List;

@Getter
@Setter
public class CategoryItemResponse {

    @JsonIgnore
    Long itemId;

    String name;

    Integer price;

    Integer discountedPrice;

    List<RelatedColorItemResponse> relatedColorItems;


    public CategoryItemResponse(Item item) {
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

    /*
    public CategoryItemResponse(Item item, Set<ColorItem> relatedColorItems) {
        name = item.getName();
        this.relatedColorItems.addAll(relatedColorItems.stream().map(RelatedCategoryItemResponse::new).toList());
    }*/
}
