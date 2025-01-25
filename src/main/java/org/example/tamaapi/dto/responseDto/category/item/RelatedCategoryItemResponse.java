package org.example.tamaapi.dto.responseDto.category.item;

import lombok.Getter;
import org.example.tamaapi.domain.ColorItem;
import org.example.tamaapi.domain.ItemImage;
import org.example.tamaapi.dto.responseDto.item.ItemDto;
import org.example.tamaapi.dto.responseDto.item.ItemStockDto;
import org.example.tamaapi.dto.responseDto.item.RelatedColorItemDto;

import java.util.ArrayList;
import java.util.List;

@Getter
public class RelatedCategoryItemResponse {
    //ColorItemId
    Long id;

    String color;

    String image;

    public RelatedCategoryItemResponse(ColorItem colorItem) {
        id = colorItem.getId();
        color = colorItem.getColor().getName();
        image = colorItem.getImageSrc();
    }
}
