package org.example.tamaapi.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.example.tamaapi.domain.item.ColorItem;
import org.example.tamaapi.domain.item.ColorItemImage;
import org.example.tamaapi.domain.item.ColorItemSizeStock;
import org.example.tamaapi.domain.item.Item;
import org.example.tamaapi.exception.MyBadRequestException;
import org.example.tamaapi.repository.JdbcTemplateRepository;
import org.example.tamaapi.repository.item.ColorItemRepository;
import org.example.tamaapi.repository.item.ColorItemSizeStockRepository;
import org.example.tamaapi.repository.item.ItemRepository;
import org.example.tamaapi.util.ErrorMessageUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.example.tamaapi.util.ErrorMessageUtil.*;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final JdbcTemplateRepository jdbcTemplateRepository;
    private final ColorItemRepository colorItemRepository;
    private final EntityManager em;
    private final ColorItemSizeStockRepository colorItemSizeStockRepository;

    public List<Long> saveItem(Item item, List<ColorItem> colorItems, List<ColorItemSizeStock> colorItemSizeStocks) {
        itemRepository.save(item);
        jdbcTemplateRepository.saveColorItems(colorItems);

        //colorItems는 bulk insert해서 객체에 pk가 없음
        //colorItemSizeStocks bulk insert하려면 colorItem pk가 필요함
        //db에서 pk를 조회해서 객체에 넣어주기

        //p.s)
        //이 메서드를 호출하는 컨트롤러에서 colorItemSizeStock에 colorItem을 넣어둠
        //colorItem에 pk를 넣으면 자동으로 colorItemSizeStock의 colorItem이 채워짐 (참조 객체)
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

    //변경 감지는 갱실 분실 문제 발생 -> 직접 update로 배타적 락 이용
    public void removeStock(Long colorItemSizeStockId, int quantity){
        //동시 요청 발생하면 재고 검사 사전에 해도 못 잡음
        int updated = em.createQuery("update ColorItemSizeStock c set c.stock = c.stock-:quantity " +
                        "where c.id = :id and c.stock >= :quantity")
                .setParameter("quantity", quantity)
                .setParameter("id", colorItemSizeStockId)
                .executeUpdate();

        //재고보다 주문양이 많으면 업데이튿 된 row 없는 걸 이용
        if (updated == 0)
            throw new IllegalArgumentException("재고가 부족합니다.");
    }

    public void changeStock(Long colorItemSizeStockId, int quantity){
        ColorItemSizeStock colorItemSizeStock = colorItemSizeStockRepository.findById(colorItemSizeStockId)
                .orElseThrow(()->new IllegalArgumentException(NOT_FOUND_ITEM));
        colorItemSizeStock.changeStock(quantity);
    }



}
