package org.example.tamaapi.dto.requestDto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class OrderItemRequest {

    //colorItemSizeStockId
    @NotNull
    private Long colorItemSizeStockId;

    //orderCount
    @NotNull
    private Integer orderCount;

}
