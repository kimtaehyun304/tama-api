package org.example.tamaapi.dto.responseDto.color;

import lombok.Getter;
import org.example.tamaapi.domain.item.Color;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ColorResponse {

    private final Long id;

    private final String name;

    private final String hexCode;

    private final List<ChildColorResponse> children = new ArrayList<>();

    public ColorResponse(Color color) {
        id = color.getId();
        name = color.getName();
        hexCode = color.getHexCode();
        children.addAll(color.getChildren().stream().map(ChildColorResponse::new).toList());
    }
}
