package org.example.tamaapi.repository;

import org.example.tamaapi.domain.ColorItem;
import org.example.tamaapi.domain.Gender;
import org.example.tamaapi.domain.Item;
import org.example.tamaapi.repository.query.ItemMinMaxQueryDto;
import org.example.tamaapi.repository.query.ItemQueryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("select i from Item i join i.colorItems c join c.colorItemSizeStocks s")
    List<Item> test();

}
