package org.example.tamaapi.repository.item;

import org.example.tamaapi.domain.item.ColorItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ColorItemRepository extends JpaRepository<ColorItem, Long> {

    //아이템 상세
    @Query("select c from ColorItem c join fetch c.color cl join fetch c.item i join fetch i.category join fetch c.colorItemSizeStocks where c.id = :colorItemId")
    Optional<ColorItem> findWithItemAndStocksByColorItemId(Long colorItemId);

    //아이템 연관상품
    @Query("select c from ColorItem c join fetch c.color cl join fetch c.colorItemSizeStocks where c.item.id = :itemId")
    List<ColorItem> findRelatedColorItemByItemId(Long itemId);

    /*
    @Query("select c from ColorItem c join fetch c.item join fetch c.images where c.item.id = :itemId")
    List<ColorItem> findWithItemAndImagesByItemId(Long itemId);
    */

}
