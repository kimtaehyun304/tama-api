package org.example.tamaapi.repository.query;

import org.example.tamaapi.domain.Gender;
import org.example.tamaapi.domain.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ItemQueryRepository extends JpaRepository<Item, Long> {

    //할인중이면 할인가격이 기준임. 안하는중이면 원래 가격
    @Query("select new org.example.tamaapi.repository.query.ItemMinMaxQueryDto(MIN(COALESCE(i.discountedPrice, i.price)), MAX(COALESCE(i.discountedPrice, i.price))) " +
            "from Item i where i.category.id in :categoryIds")
    Optional<ItemMinMaxQueryDto> findMinMaxPriceByCategoryIdIn(List<Long> categoryIds);


    //카테고리 아이템 검색 필터 (페이징 용)
    //엔티티가 select절에 있어서 중복제거됨
    @Query("SELECT new org.example.tamaapi.repository.query.ItemQueryDto(i, SUM(s.stock)) " +
            "FROM Item i JOIN i.colorItems c JOIN c.stocks s " +
            "WHERE i.category.id in :categoryIds " +
            "AND (:minPrice IS NULL OR i.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR i.price <= :maxPrice) " +
            "AND (:colorIds IS NULL OR c.color.id IN :colorIds) " +
            "AND (:genders IS NULL OR i.gender IN :genders) " +
            "GROUP BY i.id, c.id HAVING SUM(s.stock) > CASE WHEN :isContainSoldOut = TRUE THEN -1 ELSE 0 END")
    Page<Item> findAllByFilterAndItemIdIn(List<Long> categoryIds, Integer minPrice, Integer maxPrice, List<Long> colorIds, List<Gender> genders, Boolean isContainSoldOut, Pageable pageable);


    /*
    //카테고리 아이템 검색 필터 (페이징 일부)
    //HAVING SUM(s.stock) > -1 #0도 포함. 즉 품절 포함
    //HAVING SUM(s.stock) > 0 #품절 포함X
    //minMaxPrice discountedPrice인지 price인지 확인 필요
    @Query("SELECT new org.example.tamaapi.repository.query.ItemQueryDto(i, c, SUM(s.stock)) " +
            "FROM Item i JOIN i.colorItems c JOIN c.stocks s " +
            "WHERE i.category.id in :categoryIds " +
            "AND (:minPrice IS NULL OR i.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR i.price <= :maxPrice) " +
            "AND (:colorIds IS NULL OR c.color.id IN :colorIds) " +
            "AND (:genders IS NULL OR i.gender IN :genders) " +
            "GROUP BY c.id HAVING SUM(s.stock) > CASE WHEN :isContainSoldOut = TRUE THEN -1 ELSE 0 END")
    Page<ItemQueryDto> findAllByFilterAndItemIdIn(List<Long> categoryIds, Integer minPrice, Integer maxPrice, List<Long> colorIds, List<Gender> genders, Boolean isContainSoldOut, Pageable pageable);
    */
}

