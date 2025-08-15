package org.example.tamaapi.dto.responseDto.item;

import lombok.Getter;
import lombok.ToString;
import org.example.tamaapi.domain.item.ColorItem;
import org.example.tamaapi.domain.item.ColorItemImage;
import org.example.tamaapi.dto.UploadFile;

import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
public class ColorItemDetailDto {

    //ColorItemId
    private final Long id;

    private final Integer originalPrice;

    private final Integer nowPrice;

    private final String color;

    //공통 정보
    private final ItemDto common;

    private final List<UploadFile> uploadFiles = new ArrayList<>();

    private final List<ColorItemSizeStockDto> sizeStocks = new ArrayList<>();

    private final List<RelatedColorItemDto> relatedColorItems = new ArrayList<>();

    // 상품 상세
    public ColorItemDetailDto(ColorItem colorItem, List<ColorItemImage> colorItemImages, List<RelatedColorItemDto> relatedColorItemDtos) {
        id = colorItem.getId();
        originalPrice = colorItem.getItem().getOriginalPrice();
        nowPrice = colorItem.getItem().getNowPrice();
        color = colorItem.getColor().getName();
        common = new ItemDto(colorItem.getItem());
        sizeStocks.addAll(colorItem.getColorItemSizeStocks().stream().map(ColorItemSizeStockDto::new).toList());
        uploadFiles.addAll(colorItemImages.stream().map(ColorItemImage::getUploadFile).toList());
        this.relatedColorItems.addAll(relatedColorItemDtos);
        //this.images.add(colorItem.getImageSrc());
        //this.images.addAll(colorItemImages.stream().map(ci -> ci.getUploadFile().getStoredFileName()).toList());

    }


}
