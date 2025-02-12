package org.example.tamaapi.dto.responseDto.review;

import lombok.Getter;
import org.example.tamaapi.domain.Review;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Getter
public class ReviewResponse {

    private ReviewMemberResponse member;

    private String option;

    private int rating;

    private String comment;

    private LocalDate createdAt;

    public ReviewResponse(Review review){
        member = new ReviewMemberResponse(review.getMember());
        option = review.getItemStock().getColorItem().getColor().getName() + "/"+ review.getItemStock().getSize();
        rating = review.getRating();
        comment = review.getComment();
        createdAt = review.getCreatedAt().toLocalDate();
    }




}
