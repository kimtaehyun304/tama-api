package org.example.tamaapi.dto.responseDto.category;

import lombok.Getter;
import org.example.tamaapi.domain.item.Category;

@Getter
public class ChildCategoryResponse {
    private final Long id;

    private final String name;

    public ChildCategoryResponse(Category category) {
        id = category.getId();
        name = category.getName();
    }
}
