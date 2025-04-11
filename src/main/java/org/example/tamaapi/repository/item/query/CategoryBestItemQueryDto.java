package org.example.tamaapi.repository.item.query;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.example.tamaapi.domain.item.ColorItem;
import org.example.tamaapi.domain.item.Item;
import org.example.tamaapi.dto.UploadFile;

@Getter
//categoryItem에서 사용
@Setter
public class CategoryBestItemQueryDto {

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

    public CategoryBestItemQueryDto(ColorItem colorItem, Item item) {
        itemId = item.getId();
        colorItemId = colorItem.getId();
        name = item.getName();
        price = item.getPrice();
        discountedPrice = item.getDiscountedPrice();
    }



}
