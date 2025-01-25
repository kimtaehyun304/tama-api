package org.example.tamaapi.dto.responseDto;

import lombok.Getter;
import lombok.ToString;
import org.example.tamaapi.domain.ItemStock;
import org.example.tamaapi.dto.responseDto.item.ItemStockDto;

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

    ItemStockDto stock;

    /*
    public ShoppingBagDto(ColorItem colorItem) {
        colorItemId = colorItem.getId();
        color = colorItem.getColor();
        name = colorItem.getItem().getName();
        itemStock = new ItemStockDto(colorItem.getStocks().get(0));
    }
     */

    public ShoppingBagDto(ItemStock itemStock) {
        colorItemId = itemStock.getColorItem().getId();
        price = itemStock.getColorItem().getItem().getPrice();
        discountedPrice = itemStock.getColorItem().getItem().getDiscountedPrice();
        color = itemStock.getColorItem().getColor().getName();
        name = itemStock.getColorItem().getItem().getName();
        image = itemStock.getColorItem().getImageSrc();
        stock = new ItemStockDto(itemStock);
    }
}
