package org.example.tamaapi.dto.responseDto.item;

import lombok.Getter;
import lombok.ToString;
import org.example.tamaapi.domain.ItemStock;


@Getter
@ToString
public class ItemStockDto {


    Long id;

    String size;

    int stock;

    public ItemStockDto(ItemStock itemStock) {
        id = itemStock.getId();
        size = itemStock.getSize();
        stock = itemStock.getStock();
    }
}
