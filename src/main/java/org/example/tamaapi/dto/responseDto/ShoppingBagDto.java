package org.example.tamaapi.dto.responseDto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.example.tamaapi.domain.item.ColorItemImage;
import org.example.tamaapi.domain.item.ColorItemSizeStock;
import org.example.tamaapi.dto.UploadFile;
import org.example.tamaapi.dto.responseDto.item.ColorItemSizeStockDto;

@Getter
@Setter
@ToString
public class ShoppingBagDto {

    // 쇼핑백 로컬스토리지 JSON = {itemStockId:1, orderCount: 1}

    private Long colorItemId;

    private Integer originalPrice;

    private Integer nowPrice;

    private String color;

    private String name;

    private UploadFile uploadFile;

    private ColorItemSizeStockDto sizeStock;

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
        originalPrice = colorItemSizeStock.getColorItem().getItem().getOriginalPrice();
        nowPrice = colorItemSizeStock.getColorItem().getItem().getNowPrice();
        color = colorItemSizeStock.getColorItem().getColor().getName();
        name = colorItemSizeStock.getColorItem().getItem().getName();
        sizeStock = new ColorItemSizeStockDto(colorItemSizeStock);
    }
}
