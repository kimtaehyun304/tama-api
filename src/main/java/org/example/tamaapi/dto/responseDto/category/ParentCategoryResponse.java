package org.example.tamaapi.dto.responseDto.category;

import lombok.Getter;
import org.example.tamaapi.domain.item.Category;

@Getter
public class ParentCategoryResponse {
    private final Long id;

    private final String name;

    public ParentCategoryResponse(Category category) {
        id = category.getId();
        name = category.getName();
    }
}
