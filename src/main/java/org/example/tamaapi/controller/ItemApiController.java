package org.example.tamaapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tamaapi.domain.*;
import org.example.tamaapi.dto.requestDto.PaginationRequest;
import org.example.tamaapi.dto.responseDto.PageCustom;
import org.example.tamaapi.dto.responseDto.category.item.CategoryItemResponse;
import org.example.tamaapi.dto.responseDto.item.ColorItemDetailDto;
import org.example.tamaapi.dto.responseDto.ShoppingBagDto;
import org.example.tamaapi.repository.*;
import org.example.tamaapi.repository.ItemImageRepository;
import org.example.tamaapi.repository.query.RelatedColorItemQueryDto;
import org.example.tamaapi.repository.query.ColorItemQueryRepository;
import org.example.tamaapi.repository.ItemRepository;
import org.example.tamaapi.repository.ItemStockRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ItemApiController {

    private final ColorItemRepository colorItemRepository;
    private final ItemImageRepository itemImageRepository;
    private final ItemStockRepository itemStockRepository;
    private final ItemRepository itemRepository;
    private final ColorItemQueryRepository colorItemQueryRepository;
    private final CategoryRepository categoryRepository;

    @GetMapping("/api/color-items/{colorItemId}")
    public ColorItemDetailDto colorItemDetail(@PathVariable Long colorItemId) {
        ColorItem colorItem = colorItemRepository.findWithItemAndStocksByColorItemId(colorItemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 colorItem을 찾을 수 없습니다"));
        List<ItemImage> itemImage = itemImageRepository.findAllByColorItemId(colorItemId);
        List<ColorItem> relatedColorItems = colorItemRepository.findAllByItemId(colorItem.getItem().getId());
        return new ColorItemDetailDto(colorItem, itemImage, relatedColorItems);
    }

    @GetMapping("/api/item-stocks")
    public List<ShoppingBagDto> shoppingBag(@RequestParam(value = "id") List<Long> itemStockIds) {
        List<ItemStock> itemStocks = itemStockRepository.findAllWithColorItemAndItemByIdIn(itemStockIds);
        return itemStocks.stream().map(ShoppingBagDto::new).toList();
    }

    //페이징은 페치조인 불가
    @GetMapping("/api/items")
    public PageCustom<CategoryItemResponse> test(@RequestParam Long categoryId, @Valid PaginationRequest paginationRequest, BindingResult bindingResult) {
        //상위 카테고리인지 확인
        Category category = categoryRepository.findWithChildrenById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 category를 찾을 수 없습니다"));
        List<Long> categoryIds = new ArrayList<>();

        //상위 카테고리는 자식을 다 보여줌 = 전체
        if(category.getChildren().isEmpty())
            categoryIds.add(categoryId);
        else
            categoryIds.addAll(category.getChildren().stream().map(Category::getId).toList());

        PageRequest pageRequest = PageRequest.of(paginationRequest.getPage()-1, paginationRequest.getSize(), Sort.by(paginationRequest.getDirection(), paginationRequest.getProperty()));

        //페이징이라 부모만 조회
        Page<Item> items = itemRepository.findAllByCategoryIdIn(categoryIds, pageRequest);
        Page<CategoryItemResponse> categoryItems = items.map(CategoryItemResponse::new);

        //페이징 -> 페이징 커스텀 변환
        PageCustom<CategoryItemResponse> customCategoryItems = new PageCustom<>(categoryItems.getContent(), categoryItems.getPageable(), categoryItems.getTotalPages());

        //자식 조회
        List<Long> itemIds = customCategoryItems.getContent().stream().map(CategoryItemResponse::getItemId).toList();
        List<RelatedColorItemQueryDto> relatedColorItemQueryDtos = colorItemQueryRepository.findAllByItemIdIn(itemIds);

        //부모에 자식 넣음
        Map<Long, List<RelatedColorItemQueryDto>> colorItemMap = relatedColorItemQueryDtos.stream()
                .collect(Collectors.groupingBy(RelatedColorItemQueryDto::getItemId));
        customCategoryItems.getContent().forEach(ci -> ci.setRelatedColorItems(colorItemMap.get(ci.getItemId())));
        return customCategoryItems;
    }


    // 임시로 모든 아이템 로직으로 사용
    @GetMapping("/api/items/best")
    public void categoryBestItem(@RequestParam String category) {
    }


}
