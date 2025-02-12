package org.example.tamaapi.repository;

import org.example.tamaapi.domain.ColorItem;
import org.example.tamaapi.repository.query.RelatedColorItemQueryDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ColorItemRepository extends JpaRepository<ColorItem, Long> {

    //아이템 상세
    @Query("select c from ColorItem c join fetch c.item join fetch c.stocks where c.id = :colorItemId")
    Optional<ColorItem> findWithItemAndStocksByColorItemId(Long colorItemId);

    //아이템 연관상품
    List<ColorItem> findAllByItemId(Long itemId);


    @Query("select c from ColorItem c join fetch c.stocks s where c.item.id in :itemsIds")
    List<ColorItem> findAllWithStockByItemIdIn(List<Long> itemsIds);

}
