package org.example.tamaapi.repository;

import org.example.tamaapi.domain.ItemStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface ItemStockRepository extends JpaRepository<ItemStock, Long> {

    // 쇼핑백
    @Query("select isk from ItemStock isk join fetch isk.colorItem c join fetch c.item i where isk.id in :ids")
    List<ItemStock> findAllWithColorItemAndItemByIdIn(List<Long> ids);

}
