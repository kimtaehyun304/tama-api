package org.example.tamaapi.dto.requestDto.item.save;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SaveSizeStockRequest {

    private String size;

    private int stock;
}
