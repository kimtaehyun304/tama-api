package org.example.tamaapi.repository.item;

import org.example.tamaapi.domain.item.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("""
            select r from Review r join fetch r.member
            join fetch r.orderItem oi join fetch oi.colorItemSizeStock isk
            join fetch isk.colorItem ci join fetch ci.color
            where ci.id =:colorItemId
            """)
    Page<Review> findReviewsByColorItemId(Long colorItemId, Pageable pageable);

    @Query("select ROUND(avg(r.rating),1) from Review r join r.orderItem oi join oi.colorItemSizeStock isk join isk.colorItem ci where ci.id =:colorItemId")
    Optional<Double> findAvgRatingByColorItemId(Long colorItemId);

    Optional<Review> findByOrderItemId(Long orderItemId);

}
