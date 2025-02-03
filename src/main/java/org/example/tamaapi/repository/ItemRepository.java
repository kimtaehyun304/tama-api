package org.example.tamaapi.repository;

import org.example.tamaapi.domain.ColorItem;
import org.example.tamaapi.domain.Gender;
import org.example.tamaapi.domain.Item;
import org.example.tamaapi.repository.query.ItemQueryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public interface ItemRepository extends JpaRepository<Item, Long> {


    @Query("select i from Item i join i.colorItems c join c.stocks s where i.category.id = :categoryId group by i.id, c.id having sum(s.stock) > 0")
    Page<Item> findAllBySumGreaterThan(Long categoryId, Pageable pageable);

    //Page<Item> findAllByCategoryIdIn(List<Long> categoryIds, Pageable pageable);
    @Query("select i from Item i where i.id in :itemIds order by ")
    Page<Item> findAllByIdIn(Set<Long> itemIds, Pageable pageable);

    @Query("select i from Item i join i.colorItems c join c.stocks s")
    List<Item> test();


}
