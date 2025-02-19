package org.example.tamaapi.repository;

import org.example.tamaapi.domain.item.ItemImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemImageRepository extends JpaRepository<ItemImage, Long> {

    List<ItemImage> findAllByColorItemId(@Param("colorItemId") Long colorItemId);

}
