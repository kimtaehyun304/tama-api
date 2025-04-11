package org.example.tamaapi.service;

import lombok.RequiredArgsConstructor;
import org.example.tamaapi.domain.item.Review;
import org.example.tamaapi.repository.item.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    //정렬 테스트용
    public void updateCreatedAt(Long reviewId){
        Review review = reviewRepository.findById(reviewId).get();
        review.setCreatedAt(LocalDateTime.now().minusDays(1));
    }
}
