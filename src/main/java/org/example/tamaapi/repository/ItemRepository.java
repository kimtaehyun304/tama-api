package org.example.tamaapi.repository;

import org.example.tamaapi.domain.item.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("select i from Item i join i.colorItems c join c.colorItemSizeStocks s")
    List<Item> test();

}
