package org.example.tamaapi.dto.responseDto.item;

import lombok.Getter;
import lombok.ToString;
import org.example.tamaapi.domain.item.ColorItem;
import org.example.tamaapi.domain.item.ItemImage;

import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
public class ColorItemDetailDto {

    //ColorItemId
    Long id;

    Integer price;

    Integer discountedPrice;

    String color;

    //공통 정보
    ItemDto common;

    List<String> images = new ArrayList<>();

    List<ColorItemSizeStockDto> sizeStocks = new ArrayList<>();

    List<RelatedColorItemDto> relatedColorItems = new ArrayList<>();

    // 상품 상세
    public ColorItemDetailDto(ColorItem colorItem, List<ItemImage> itemImages, List<ColorItem> colorItems) {
        id = colorItem.getId();
        price = colorItem.getItem().getPrice();
        discountedPrice = colorItem.getItem().getDiscountedPrice();
        color = colorItem.getColor().getName();
        common = new ItemDto(colorItem.getItem());
        sizeStocks.addAll(colorItem.getColorItemSizeStocks().stream().map(ColorItemSizeStockDto::new).toList());
        this.images.add(colorItem.getImageSrc());
        this.images.addAll(itemImages.stream().map(ItemImage::getSrc).toList());
        relatedColorItems.addAll(colorItems.stream().map(RelatedColorItemDto::new).toList());
    }


}
