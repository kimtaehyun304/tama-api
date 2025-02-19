package org.example.tamaapi.dto.requestDto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class GuestOrderRequest {

    //colorItemSizeStockId
    @NotNull
    private Long id;

    //orderCount
    @NotNull
    private Integer count;

}
