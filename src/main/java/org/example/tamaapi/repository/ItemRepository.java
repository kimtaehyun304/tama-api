package org.example.tamaapi.repository;

import org.example.tamaapi.domain.ColorItem;
import org.example.tamaapi.domain.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    //일반 조인이라 1:N *2 가능
    //최적화 방법
    //예상 in
    //실제 select from colorItem c where c.item.id = ? (item 1개라 그랬음)
    @Query("select i from Item i join i.colorItems c join c.stocks s where i.category.id = :categoryId group by i.id, c.id having sum(s.stock) > 0")
    Page<Item> findAllBySumGreaterThan(Long categoryId, Pageable pageable);

    //Page<Item> findAllByCategoryId(Long categoryId, Pageable pageable);

    Page<Item> findAllByCategoryIdIn(List<Long> categoryIds, Pageable pageable);
}
