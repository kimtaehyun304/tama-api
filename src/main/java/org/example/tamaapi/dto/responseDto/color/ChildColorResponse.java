package org.example.tamaapi.dto.responseDto.color;

import lombok.Getter;
import org.example.tamaapi.domain.item.Color;

@Getter
public class ChildColorResponse {

    private Long id;

    private String name;

    private String hexCode;



    public ChildColorResponse(Color color) {
        id = color.getId();
        name = color.getName();
        hexCode = color.getHexCode();
    }
}
