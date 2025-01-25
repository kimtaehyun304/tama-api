package org.example.tamaapi.dto.responseDto.category;

import lombok.Getter;
import org.example.tamaapi.domain.Category;

import java.util.List;

@Getter
public class ChildCategoryResponse {
    Long id;

    String name;

    public ChildCategoryResponse(Category category) {
        id = category.getId();
        name = category.getName();
    }
}
