package org.example.tamaapi.dto.responseDto.category;

import lombok.Getter;
import org.example.tamaapi.domain.item.Category;

import java.util.List;

@Getter
public class ParentCategoryResponse {
    private Long id;

    private String name;

    public ParentCategoryResponse(Category category) {
        id = category.getId();
        name = category.getName();
    }
}
