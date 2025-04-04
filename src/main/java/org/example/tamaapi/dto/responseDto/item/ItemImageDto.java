package org.example.tamaapi.dto.responseDto.item;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tamaapi.domain.item.ColorItem;
import org.example.tamaapi.domain.item.ColorItemImage;
import org.example.tamaapi.dto.UploadFile;


@Getter
// 대표 이미지 이외 저장
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemImageDto {

    private Long id;

    private Long colorItemId;

    private UploadFile uploadFile;

    private Integer sequence;

    public ItemImageDto(ColorItemImage colorItemImage) {
        id = colorItemImage.getId();
        colorItemId = colorItemImage.getColorItem().getId();
        uploadFile = colorItemImage.getUploadFile();
        sequence = colorItemImage.getSequence();
    }
}
