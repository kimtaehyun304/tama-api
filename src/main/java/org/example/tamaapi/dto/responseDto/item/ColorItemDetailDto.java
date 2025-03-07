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
    private Long id;

    private Integer price;

    private Integer discountedPrice;

    private String color;

    //공통 정보
    private ItemDto common;

    private List<String> images = new ArrayList<>();

    private List<ColorItemSizeStockDto> sizeStocks = new ArrayList<>();

    private List<RelatedColorItemDto> relatedColorItems = new ArrayList<>();

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
