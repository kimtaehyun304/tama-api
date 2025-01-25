package org.example.tamaapi.repository.query;

import org.example.tamaapi.domain.Item;
import org.example.tamaapi.dto.responseDto.category.item.CategoryItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ColorItemQueryRepository extends JpaRepository<Item, Long> {

    /*
    @Query("select new org.example.tamaapi.dto.responseDto.category.item.CategoryItemResponse(i, c, sum(s.stock)) from Item i " +
            "join i.colorItems c join c.stocks s where i.category.id = :categoryId group by c.id having sum(s.stock) > 0")
    Page<CategoryItemResponse> findAllBySumGreaterThan(Long categoryId, Pageable pageable);
    */

    //사이즈와 상관없이 재고 하나라도 있는 colorItem 조회
    @Query("select new org.example.tamaapi.repository.query.RelatedColorItemQueryDto(c, sum(s.stock)) from ColorItem c " +
            "join c.stocks s where c.item.id in :itemsIds group by c.id having sum(s.stock) > 0")
    List<RelatedColorItemQueryDto> findAllByItemIdIn(List<Long> itemsIds);





}
