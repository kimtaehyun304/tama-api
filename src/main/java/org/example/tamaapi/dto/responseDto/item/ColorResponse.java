package org.example.tamaapi.dto.responseDto.item;

import lombok.Getter;
import org.example.tamaapi.domain.Category;
import org.example.tamaapi.domain.Color;
import org.example.tamaapi.dto.responseDto.category.ChildCategoryResponse;
import org.example.tamaapi.util.ColorUtil;

import java.util.List;

@Getter
public class ColorResponse {
    Long id;

    String name;

    String hexCode;
    public ColorResponse(Color color) {
        id = color.getId();
        name = color.getName();
        hexCode = color.getHexCode();
    }
}
