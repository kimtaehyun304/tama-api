package org.example.tamaapi.repository.item;

import org.example.tamaapi.domain.item.ColorItemSizeStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ColorItemSizeStockRepository extends JpaRepository<ColorItemSizeStock, Long> {

    // 쇼핑백
    @Query("select isk from ColorItemSizeStock isk join fetch isk.colorItem c join fetch c.color cl join fetch c.item i where isk.id in :ids")
    List<ColorItemSizeStock> findAllWithColorItemAndItemByIdIn(List<Long> ids);


    List<ColorItemSizeStock> findAllByColorItemIdIn(List<Long> colorItemIds);

}
