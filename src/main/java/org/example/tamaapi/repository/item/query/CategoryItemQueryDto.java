package org.example.tamaapi.repository.item.query;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.example.tamaapi.domain.item.ColorItem;
import org.example.tamaapi.domain.item.Item;
import org.example.tamaapi.dto.responseDto.category.item.RelatedColorItemResponse;

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


    public CategoryItemQueryDto(Item item) {
        itemId = item.getId();
        name = item.getName();
        price = item.getPrice();
        discountedPrice = item.getDiscountedPrice();
    }

    public CategoryItemQueryDto(Item item, ColorItem colorItem) {
        itemId = item.getId();
        name = item.getName();
        price = item.getPrice();
        discountedPrice = item.getDiscountedPrice();
    }


    public CategoryItemQueryDto(Item item, ColorItem colorItem, Long totalStock) {
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
