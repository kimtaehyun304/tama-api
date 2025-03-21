package org.example.tamaapi.repository.item.query;

import lombok.Getter;

@Getter
public class CategoryBestItemReviewQueryDto {

    private Long colorItemId;

    private Double avgRating;

    private Long reviewCount;

    public CategoryBestItemReviewQueryDto(Long colorItemId, Double avgRating, Long reviewCount) {
        this.colorItemId = colorItemId;
        this.avgRating = avgRating;
        this.reviewCount = reviewCount;
    }
}
