package org.example.tamaapi.dto.responseDto.item;

import lombok.Getter;
import lombok.ToString;
import org.example.tamaapi.domain.item.ColorItem;

@Getter
@ToString
public class RelatedColorItemDto {

    //ColorItemId
    Long id;

    String color;

    String imageSrc;

    public RelatedColorItemDto(ColorItem colorItem) {
        id = colorItem.getId();
        color = colorItem.getColor().getName();
        imageSrc = colorItem.getImageSrc();
    }

}
