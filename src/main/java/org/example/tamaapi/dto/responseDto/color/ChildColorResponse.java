package org.example.tamaapi.dto.responseDto.color;

import lombok.Getter;
import org.example.tamaapi.domain.item.Color;

@Getter
public class ChildColorResponse {

    private final Long id;

    private final String name;

    private final String hexCode;



    public ChildColorResponse(Color color) {
        id = color.getId();
        name = color.getName();
        hexCode = color.getHexCode();
    }
}
