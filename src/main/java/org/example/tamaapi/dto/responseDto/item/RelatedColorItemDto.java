package org.example.tamaapi.dto.responseDto.item;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.example.tamaapi.domain.item.ColorItem;
import org.example.tamaapi.dto.UploadFile;

@Getter
@ToString
@Setter
public class RelatedColorItemDto {

    //ColorItemId
    private Long id;

    private String color;

    private UploadFile uploadFile;

    public RelatedColorItemDto(ColorItem colorItem, UploadFile uploadFile) {
        id = colorItem.getId();
        color = colorItem.getColor().getName();
        this.uploadFile = uploadFile;
    }

}
