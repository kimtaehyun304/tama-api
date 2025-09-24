package org.example.tamaapi.repository.item;

import org.example.tamaapi.domain.item.Item;
import org.example.tamaapi.repository.item.query.dto.CategoryBestItemQueryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("select i from Item i join fetch i.colorItems where i.name=:name")
    Optional<Item> findWithColorItemByName(String name);


    /*field는 mysql 전용 함수 -> 네이티브 쿼리 써야함
    @Query("select i from Item i order by field(:itemIds)")
    List<Item> findAllOrderBy(List<Long> itemIds);
    */


}
