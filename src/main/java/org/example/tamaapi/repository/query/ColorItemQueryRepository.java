package org.example.tamaapi.repository.query;

import org.example.tamaapi.domain.ColorItem;
import org.example.tamaapi.domain.Gender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

//DTO 반환이라 extends 엔티티에 맞는게 없음. -> 아무거나 써도 에러 안남. 무난하게 루트 엔티티 적어둠
//DATA JPA 안쓰는 게 어울리나, 순수 JPA는 생산성이 낮아서 안쓰기로 함
public interface ColorItemQueryRepository extends JpaRepository<ColorItem, Long> {

    /*
    @Query("select new org.example.tamaapi.dto.responseDto.category.item.CategoryItemResponse(i, c, sum(s.stock)) from Item i " +
            "join i.colorItems c join c.stocks s where i.category.id = :categoryId group by c.id having sum(s.stock) > 0")
    Page<CategoryItemResponse> findAllBySumGreaterThan(Long categoryId, Pageable pageable);
    */

    //카테고리 아이템 페이징 쿼리 일부
    //사이즈와 상관없이 재고 하나라도 있는 colorItem 조회
    @Query("select new org.example.tamaapi.repository.query.RelatedColorItemQueryDto(c, sum(s.stock)) from ColorItem c " +
            "join c.stocks s where c.item.id in :itemsIds group by c.id having sum(s.stock) > 0")
    List<RelatedColorItemQueryDto> findAllByItemIdIn(List<Long> itemsIds);


    //카테고리 아이템 검색 필터 (페이징 일부)
    //HAVING SUM(s.stock) > -1 #0도 포함. 즉 품절 포함
    //HAVING SUM(s.stock) > 0 #품절 포함X
    //minMaxPrice discountedPrice인지 price인지 확인 필요
    @Query("SELECT new org.example.tamaapi.repository.query.RelatedColorItemQueryDto(c, SUM(s.stock)) " +
            "FROM ColorItem c JOIN c.item i JOIN c.stocks s " +
            "WHERE c.item.id IN :itemsIds " +
            "AND (:minPrice IS NULL OR i.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR i.price <= :maxPrice) " +
            "AND (:colorIds IS NULL OR c.color.id IN :colorIds) " +
            "AND (:genders IS NULL OR i.gender IN :genders) " +
            "GROUP BY c.id HAVING SUM(s.stock) > CASE WHEN :isContainSoldOut = TRUE THEN -1 ELSE 0 END")
    List<RelatedColorItemQueryDto> findAllByFilterAndItemIdIn(List<Long> itemsIds, Integer minPrice, Integer maxPrice, List<Long> colorIds, List<Gender> genders, Boolean isContainSoldOut);


}
