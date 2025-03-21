package org.example.tamaapi.dto.responseDto.category.item;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.example.tamaapi.domain.item.ColorItem;
import org.example.tamaapi.domain.item.Item;
import org.example.tamaapi.repository.item.query.CategoryBestItemQueryDto;
import org.example.tamaapi.repository.item.query.CategoryBestItemReviewQueryDto;

import java.util.List;


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
