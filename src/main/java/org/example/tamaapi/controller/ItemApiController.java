package org.example.tamaapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tamaapi.domain.item.*;
import org.example.tamaapi.dto.requestDto.CategoryItemFilterRequest;
import org.example.tamaapi.dto.requestDto.MyPageRequest;
import org.example.tamaapi.dto.requestDto.MySort;
import org.example.tamaapi.dto.responseDto.MyPage;
import org.example.tamaapi.dto.responseDto.category.item.CategoryItemResponse;
import org.example.tamaapi.dto.responseDto.category.item.RelatedColorItemResponse;
import org.example.tamaapi.dto.responseDto.item.ColorItemDetailDto;
import org.example.tamaapi.dto.responseDto.ShoppingBagDto;
import org.example.tamaapi.dto.validator.SortValidator;
import org.example.tamaapi.exception.MyBadRequestException;
import org.example.tamaapi.repository.*;
import org.example.tamaapi.repository.ItemImageRepository;
import org.example.tamaapi.repository.query.*;
import org.example.tamaapi.repository.ItemRepository;
import org.example.tamaapi.repository.ColorItemSizeStockRepository;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ItemApiController {

    private final ColorItemRepository colorItemRepository;
    private final ItemImageRepository itemImageRepository;
    private final ColorItemSizeStockRepository colorItemSizeStockRepository;
    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final ItemQueryRepository itemQueryRepository;
    private final ColorRepository colorRepository;
    private final SortValidator sortValidator;

    @GetMapping("/api/colorItems/{colorItemId}")
    public ColorItemDetailDto colorItemDetail(@PathVariable Long colorItemId) {
        ColorItem colorItem = colorItemRepository.findWithItemAndStocksByColorItemId(colorItemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 colorItem을 찾을 수 없습니다"));
        List<ItemImage> itemImage = itemImageRepository.findAllByColorItemId(colorItemId);
        List<ColorItem> relatedColorItems = colorItemRepository.findAllByItemId(colorItem.getItem().getId());
        return new ColorItemDetailDto(colorItem, itemImage, relatedColorItems);
    }

    @GetMapping("/api/colorItemSizeStock")
    public List<ShoppingBagDto> shoppingBag(@RequestParam(value = "id") List<Long> itemStockIds) {
        List<ColorItemSizeStock> colorItemSizeStocks = colorItemSizeStockRepository.findAllWithColorItemAndItemByIdIn(itemStockIds);
        return colorItemSizeStocks.stream().map(ShoppingBagDto::new).toList();
    }

    //sort는 if문 검증이라 분리
    @GetMapping("/api/items")
    public MyPage<CategoryItemResponse> categoryItem(@RequestParam Long categoryId, @Valid MyPageRequest myPageRequest
            , @RequestParam MySort sort, @Valid CategoryItemFilterRequest itemFilter) {

        if(itemFilter.getMinPrice()!= null && itemFilter.getMaxPrice() != null && itemFilter.getMinPrice() > itemFilter.getMaxPrice())
                throw new MyBadRequestException("최소값을 최대값보다 크게 입력했습니다.");

        sortValidator.validate(sort);

        //상위 카테고리인지 확인
        Category category = categoryRepository.findWithChildrenById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 category를 찾을 수 없습니다"));
        List<Long> categoryIds = new ArrayList<>();

        //상위 카테고리일경우 하위를 함께 보여줌
        if(category.getChildren().isEmpty())
            categoryIds.add(categoryId);
        else
            categoryIds.addAll(category.getChildren().stream().map(Category::getId).toList());

        //상위 색상일경우 하위를 함께 보여줌
        List<Long> colorIds = new ArrayList<>();
        if(itemFilter != null){
            List<Color> colors = colorRepository.findWithChildrenByIdIn(itemFilter.getColorIds());
            for (Color color : colors) {
                colorIds.add(color.getId());
                colorIds.addAll(color.getChildren().stream().map(Color::getId).toList());
            }
        }

        //다용도
        List<Item> items = itemQueryRepository.findItemsByCategoryIdInAndFilter(categoryIds, itemFilter.getMinPrice(), itemFilter.getMaxPrice(), colorIds
                , itemFilter.getGenders(), itemFilter.getIsContainSoldOut());
        //페이징
        List<Long> itemIds = items.stream().map(Item::getId).toList();
        List<CategoryItemResponse> categoryItems = itemQueryRepository.findAllByItemIdIn(itemIds, sort, myPageRequest);

        //커스텀 페이징 변환
        int rowCount = items.size();
        MyPage<CategoryItemResponse> myCategoryItems = new MyPage<>(categoryItems, myPageRequest, rowCount);

        List<RelatedColorItemResponse> colorItems = itemQueryRepository.findColorItemsByCategoryIdInAndFilter(itemIds, colorIds, itemFilter.getIsContainSoldOut());

        //key:itemId. List<CategoryItemResponse>에 List<RelatedColorItemResponse> 삽입
        Map<Long, List<RelatedColorItemResponse>> colorItemMap = colorItems.stream().collect(Collectors.groupingBy(RelatedColorItemResponse::getItemId));
        myCategoryItems.getContent().forEach(ci -> ci.setRelatedColorItems(colorItemMap.get(ci.getItemId())));

        return myCategoryItems;
    }

    @GetMapping("/api/items/minMaxPrice")
    public ItemMinMaxQueryDto categoryItemMinMaxPrice(@RequestParam Long categoryId, @Valid CategoryItemFilterRequest itemFilter) {

        //상위 카테고리인지 확인
        Category category = categoryRepository.findWithChildrenById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 category를 찾을 수 없습니다"));
        List<Long> categoryIds = new ArrayList<>();

        //상위 카테고리일경우 하위를 함께 보여줌
        if(category.getChildren().isEmpty())
            categoryIds.add(categoryId);
        else
            categoryIds.addAll(category.getChildren().stream().map(Category::getId).toList());

        //상위 색상일경우 하위를 함께 보여줌
        List<Long> colorIds = new ArrayList<>();
        if(itemFilter != null){
            List<Color> colors = colorRepository.findWithChildrenByIdIn(itemFilter.getColorIds());
            for (Color color : colors) {
                colorIds.add(color.getId());
                colorIds.addAll(color.getChildren().stream().map(Color::getId).toList());
            }
        }

        ItemMinMaxQueryDto itemMinMaxQueryDto = itemQueryRepository.findMinMaxPriceByCategoryIdInAndFilter(categoryIds, itemFilter.getMinPrice(), itemFilter.getMaxPrice(), colorIds
                , itemFilter.getGenders(), itemFilter.getIsContainSoldOut()).orElseThrow(() -> new IllegalArgumentException("해당 아이템 최소가격 최대가격을 찾을 수 없습니다"));

        return itemMinMaxQueryDto;
    }

    @GetMapping("/test")
    public int test(){
        List<Item> test = itemRepository.test();
        return test.size();
    }

    // 임시로 모든 아이템 로직으로 사용
    @GetMapping("/api/items/best")
    public void categoryBestItem(@RequestParam String category) {
    }


}
