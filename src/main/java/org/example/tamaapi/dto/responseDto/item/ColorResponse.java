package org.example.tamaapi.dto.responseDto.item;

import lombok.Getter;
import org.example.tamaapi.domain.item.Color;

@Getter
public class ColorResponse {

    private Long id;

    private String name;

    private String hexCode;
    public ColorResponse(Color color) {
        id = color.getId();
        name = color.getName();
        hexCode = color.getHexCode();
    }
}
