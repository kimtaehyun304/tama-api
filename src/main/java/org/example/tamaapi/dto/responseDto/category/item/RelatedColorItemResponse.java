package org.example.tamaapi.dto.responseDto.category.item;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.example.tamaapi.domain.item.ColorItem;
import org.example.tamaapi.dto.UploadFile;

@Getter
@Setter
//categoryItem에서 사용
public class RelatedColorItemResponse {

    @JsonIgnore
    private Long itemId;

    private Long colorItemId;

    private String color;

    private String hexCode;

    //대표 이미지
    private UploadFile uploadFile;

    //모든 사이즈 재고 합계
    private int totalStock;

    //@JsonIgnore
    //private LocalDateTime createdAt;

    public RelatedColorItemResponse(ColorItem colorItem, Long totalStock) {
        itemId = colorItem.getItem().getId();
        colorItemId = colorItem.getId();
        color = colorItem.getColor().getName();
        hexCode = colorItem.getColor().getHexCode();
        this.totalStock = (int) totalStock.longValue();
        //createdAt = colorItem.getCreatedAt();
    }

}
