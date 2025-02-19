package org.example.tamaapi.dto.responseDto;

import lombok.Getter;
import lombok.ToString;
import org.example.tamaapi.domain.item.ColorItemSizeStock;
import org.example.tamaapi.dto.responseDto.item.ColorItemSizeStockDto;

@Getter
@ToString
public class ShoppingBagDto {

    // 쇼핑백 로컬스토리지 JSON = {itemStockId:1, orderCount: 1}

    Long colorItemId;

    Integer price;

    Integer discountedPrice;

    String color;

    String name;

    String image;

    ColorItemSizeStockDto sizeStock;

    /*
    public ShoppingBagDto(ColorItem colorItem) {
        colorItemId = colorItem.getId();
        color = colorItem.getColor();
        name = colorItem.getItem().getName();
        itemStock = new ItemStockDto(colorItem.getStocks().get(0));
    }
     */

    public ShoppingBagDto(ColorItemSizeStock colorItemSizeStock) {
        colorItemId = colorItemSizeStock.getColorItem().getId();
        price = colorItemSizeStock.getColorItem().getItem().getPrice();
        discountedPrice = colorItemSizeStock.getColorItem().getItem().getDiscountedPrice();
        color = colorItemSizeStock.getColorItem().getColor().getName();
        name = colorItemSizeStock.getColorItem().getItem().getName();
        image = colorItemSizeStock.getColorItem().getImageSrc();
        sizeStock = new ColorItemSizeStockDto(colorItemSizeStock);
    }
}
