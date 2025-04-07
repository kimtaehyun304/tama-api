package org.example.tamaapi.dto.responseDto.review;

import lombok.Getter;
import org.example.tamaapi.domain.item.ColorItemSizeStock;
import org.example.tamaapi.domain.item.Review;

import java.time.LocalDate;


@Getter
public class ReviewResponse {

    private final ReviewMemberResponse member;

    private final String option;

    private final int rating;

    private final String comment;

    private final LocalDate createdAt;

    public ReviewResponse(Review review){
        member = new ReviewMemberResponse(review.getMember());
        ColorItemSizeStock colorItemSizeStock = review.getOrderItem().getColorItemSizeStock();
        option = colorItemSizeStock.getColorItem().getColor().getName() + "/"+ colorItemSizeStock.getSize();
        rating = review.getRating();
        comment = review.getComment();
        createdAt = review.getCreatedAt().toLocalDate();
    }




}
