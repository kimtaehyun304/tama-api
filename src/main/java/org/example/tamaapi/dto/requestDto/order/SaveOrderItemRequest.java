package org.example.tamaapi.dto.requestDto.order;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SaveOrderItemRequest {

    //colorItemSizeStockId
    @NotNull
    private Long colorItemSizeStockId;

    //orderCount
    @NotNull
    private Integer orderCount;

}
