package org.example.tamaapi.dto.responseDto.category.item;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CategoryBestItemResponse {
    /*
    //@JsonIgnore
    //private Long itemId;

    private Long colorItemId;

    private String name;

    private Integer price;

    private Integer discountedPrice;

    private String imageSrc;

    private Double avgRating = 0D;

    private Long reviewCount = 0L;

    //avgRating은 컨트롤러에서 채움
    public CategoryBestItemResponse(CategoryBestItemQueryDto categoryBestItemQueryDto) {
        colorItemId = categoryBestItemQueryDto.getColorItemId();
        name = categoryBestItemQueryDto.getName();
        price = categoryBestItemQueryDto.getPrice();
        discountedPrice = categoryBestItemQueryDto.getDiscountedPrice();
        imageSrc = categoryBestItemQueryDto.getImageSrc();
    }

    public void setReview(CategoryBestItemReviewQueryDto dto){
            this.avgRating = dto.getAvgRating();
            this.reviewCount = dto.getReviewCount();
    }

     */
}
