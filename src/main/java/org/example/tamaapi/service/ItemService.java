package org.example.tamaapi.service;

import lombok.RequiredArgsConstructor;
import org.example.tamaapi.domain.item.ColorItem;
import org.example.tamaapi.domain.item.ColorItemImage;
import org.example.tamaapi.domain.item.ColorItemSizeStock;
import org.example.tamaapi.domain.item.Item;
import org.example.tamaapi.repository.JdbcTemplateRepository;
import org.example.tamaapi.repository.item.ColorItemRepository;
import org.example.tamaapi.repository.item.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final JdbcTemplateRepository jdbcTemplateRepository;
    private final ColorItemRepository colorItemRepository;

    public List<Long> saveItem(Item item, List<ColorItem> colorItems, List<ColorItemSizeStock> colorItemSizeStocks) {
        itemRepository.save(item);
        //PK 채우려고 jdbcTemplate 안씀
        //colorItemRepository.saveAll(colorItems);
        jdbcTemplateRepository.saveColorItems(colorItems);

        List<Long> colorIds = colorItems.stream().map(c -> c.getColor().getId()).toList();
        List<ColorItem> foundColorItems = colorItemRepository.findAllByItemIdAndColorIdIn(item.getId(), colorIds);

        //K:ColorId, V:colorItemId
        Map<Long, Long> map = foundColorItems.stream()
                .collect(Collectors.toMap(
                        ci -> ci.getColor().getId(),
                        ColorItem::getId
                ));

        for (ColorItem colorItem : colorItems) {
            Long savedColorItemId = map.get(colorItem.getColor().getId());
            colorItem.setIdAfterBatch(savedColorItemId);
        }

        jdbcTemplateRepository.saveColorItemSizeStocks(colorItemSizeStocks);
        return colorItems.stream().map(ColorItem::getId).toList();

    }



    public void saveColorItemImages(List<ColorItemImage> colorItemImages) {
        jdbcTemplateRepository.saveColorItemImages(colorItemImages);
    }


}
