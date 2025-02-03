package org.example.tamaapi.dto.responseDto.category.item;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.example.tamaapi.domain.ColorItem;

@Getter
//categoryItem에서 사용
public class RelatedColorItemResponse {

    @JsonIgnore
    Long itemId;

    Long colorItemId;

    String color;

    String hexCode;

    //대표 이미지
    String imageSrc;

    //모든 사이즈 재고 합계
    int totalStock;

    public RelatedColorItemResponse(ColorItem colorItem, Long totalStock) {
        itemId = colorItem.getItem().getId();
        colorItemId = colorItem.getId();
        color = colorItem.getColor().getName();
        hexCode = colorItem.getColor().getHexCode();
        imageSrc = colorItem.getImageSrc();
        this.totalStock = (int) totalStock.longValue();
    }

}
