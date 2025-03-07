package org.example.tamaapi.dto.responseDto.category;

import lombok.Getter;
import org.example.tamaapi.domain.item.Category;

import java.util.List;

@Getter
public class CategoryResponse {
    private Long id;

    private String name;

    private List<ChildCategoryResponse> children;

    public CategoryResponse(Category category) {
        id = category.getId();
        name = category.getName();
        children = category.getChildren().stream().map(ChildCategoryResponse::new).toList();
    }
}
