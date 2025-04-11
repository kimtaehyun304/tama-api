package org.example.tamaapi.dto.responseDto.item;

import lombok.Getter;
import lombok.ToString;
import org.example.tamaapi.domain.item.ColorItemSizeStock;


@Getter
@ToString
public class ColorItemSizeStockDto {

    private final Long id;

    private final String size;

    private final int stock;

    public ColorItemSizeStockDto(ColorItemSizeStock colorItemSizeStock) {
        id = colorItemSizeStock.getId();
        size = colorItemSizeStock.getSize();
        stock = colorItemSizeStock.getStock();
    }
}
