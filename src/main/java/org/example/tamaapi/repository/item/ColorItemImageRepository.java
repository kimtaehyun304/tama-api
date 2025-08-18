package org.example.tamaapi.repository.item;

import org.example.tamaapi.domain.item.ColorItemImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ColorItemImageRepository extends JpaRepository<ColorItemImage, Long> {

    List<ColorItemImage> findAllByColorItemId(Long colorItemId);

    List<ColorItemImage> findAllByColorItemIdInAndSequence(List<Long> colorItemIds, Integer sequence);


    List<ColorItemImage> findAllByColorItemItemIdInAndSequence(List<Long> itemIds, Integer sequence);

    Optional<ColorItemImage> findByColorItemIdAndSequence(Long colorItemId, Integer sequence);
}
