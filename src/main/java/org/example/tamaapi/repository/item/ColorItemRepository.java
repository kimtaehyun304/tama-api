package org.example.tamaapi.repository.item;

import org.example.tamaapi.domain.item.ColorItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ColorItemRepository extends JpaRepository<ColorItem, Long> {

    //아이템 상세
    @Query("select c from ColorItem c join fetch c.item join fetch c.colorItemSizeStocks where c.id = :colorItemId")
    Optional<ColorItem> findWithItemAndStocksByColorItemId(Long colorItemId);

    //아이템 연관상품
    List<ColorItem> findAllByItemId(Long itemId);
    

}
