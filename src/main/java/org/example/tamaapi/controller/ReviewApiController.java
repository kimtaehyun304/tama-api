package org.example.tamaapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tamaapi.domain.*;
import org.example.tamaapi.dto.requestDto.MyPageRequest;
import org.example.tamaapi.dto.requestDto.MySort;
import org.example.tamaapi.dto.responseDto.MyPageReview;
import org.example.tamaapi.dto.responseDto.review.ReviewResponse;
import org.example.tamaapi.dto.validator.SortValidator;
import org.example.tamaapi.exception.MyBadRequestException;
import org.example.tamaapi.repository.*;
import org.example.tamaapi.util.TypeConverter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReviewApiController {

    private final ReviewRepository reviewRepository;
    private final ColorItemRepository colorItemRepository;
    private final SortValidator sortValidator;

    @GetMapping("/api/reviews")
    public MyPageReview<ReviewResponse> colorItemDetail(@RequestParam Long colorItemId, @Valid MyPageRequest myPageRequest, @RequestParam MySort sort) {

        if(!sort.getProperty().equals("createdAt"))
            throw new MyBadRequestException("유효한 property가 아닙니다");

        sortValidator.validate(sort);
        Long itemId = colorItemRepository.findWithItemAndStocksByColorItemId(colorItemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 colorItem을 찾을 수 없습니다")).getItem().getId();

        Double avgRating = reviewRepository.findAvgRatingByItemId(itemId).orElse(0.0);
        Sort.Direction direction = TypeConverter.convertStringToDirection(sort.getDirection());

        PageRequest pageRequest = PageRequest.of(myPageRequest.getPage()-1, myPageRequest.getSize()
                , Sort.by(new Sort.Order(direction, sort.getProperty()), new Sort.Order(Sort.Direction.DESC, "id")));
        Page<Review> reviews = reviewRepository.findAllWithMemberWithItemStockWithColorItemByItemId(itemId, pageRequest);

        List<ReviewResponse> reviewResponses = reviews.stream().map(ReviewResponse::new).toList();
        return new MyPageReview<>(avgRating, reviewResponses, reviews.getPageable(), reviews.getTotalPages(), reviews.getTotalElements());
    }

}
