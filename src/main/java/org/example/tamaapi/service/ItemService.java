package org.example.tamaapi.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.example.tamaapi.domain.item.ColorItem;
import org.example.tamaapi.domain.item.ColorItemImage;
import org.example.tamaapi.domain.item.ColorItemSizeStock;
import org.example.tamaapi.domain.item.Item;
import org.example.tamaapi.exception.NotEnoughStockException;
import org.example.tamaapi.repository.JdbcTemplateRepository;
import org.example.tamaapi.repository.item.ColorItemRepository;
import org.example.tamaapi.repository.item.ColorItemSizeStockRepository;
import org.example.tamaapi.repository.item.ItemRepository;
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
        //colorItems 객체는 bulk insert해서 PK 없는 상태
        jdbcTemplateRepository.saveColorItems(colorItems);

        //colorItem PK를 외래키로 쓰는 colorItemSizeStock을 저장하려면 PK 필요
        //colorItemSizeStock은 colorItem의 주소 값을 갖는 상태
        //즉, colorItem PK를 채우면 colorItemSizeStock 외래키도 채워짐
        List<Long> colorIds = colorItems.stream().map(c -> c.getColor().getId()).toList();

        //방금 bulk insert한 colorItem PK 조회
        List<ColorItem> foundColorItems = colorItemRepository.findAllByItemIdAndColorIdIn(item.getId(), colorIds);

        //KEY:ColorId, VALUE:colorItemId
        Map<Long, Long> map = foundColorItems.stream()
                .collect(Collectors.toMap(
                        ci -> ci.getColor().getId(),
                        ColorItem::getId
                ));

        //colorItem PK 채우기
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

    public void removeStock(Long colorItemSizeStockId, int quantity){
        //동시에 요청 오면, UPDATE 전에 재고 조회하는 게 의미가 없음
        //단일 요청이면 의미 있긴한데, 밑에 update 쿼리만으로 재고 부족 예외 throw 가능
        //그래서 if(db.stock - quantity < 0) throw 로직 제거

        //변경 감지는 갱실 분실 문제 발생 -> 직접 update로 배타적 락으로 예방
        int updated = em.createQuery("update ColorItemSizeStock c set c.stock = c.stock-:quantity " +
                        "where c.id = :id and c.stock >= :quantity")
                .setParameter("quantity", quantity)
                .setParameter("id", colorItemSizeStockId)
                .executeUpdate();

        //재고보다 주문양이 많으면 업데이트 된 row 없는 걸 이용
        if (updated == 0)
            throw new NotEnoughStockException();
    }

    public void changeStock(Long colorItemSizeStockId, int quantity){
        ColorItemSizeStock colorItemSizeStock = colorItemSizeStockRepository.findById(colorItemSizeStockId)
                .orElseThrow(()->new IllegalArgumentException(NOT_FOUND_ITEM));
        colorItemSizeStock.changeStock(quantity);
    }

}
