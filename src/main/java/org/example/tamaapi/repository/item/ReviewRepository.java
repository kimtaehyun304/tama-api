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

    /*
    //다대일 조인이라 페이징 문제 없음
    //추천순 정렬은 복잡해서 안함
    @Query("select r from Review r join fetch r.member join fetch r.orderItem oi join fetch oi.colorItemSizeStock isk join fetch isk.colorItem ci where ci.item.id =:itemId")
    Page<Review> findAllWithMemberWithItemStockWithColorItemByItemId(Long itemId, Pageable pageable);

    @Query("select ROUND(avg(r.rating),1) from Review r join r.orderItem oi join oi.colorItemSizeStock isk join isk.colorItem ci where ci.item.id =:itemId")
    Optional<Double> findAvgRatingByItemId(Long itemId);
    */

}
