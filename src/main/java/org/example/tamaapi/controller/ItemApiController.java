package org.example.tamaapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tamaapi.domain.*;
import org.example.tamaapi.dto.requestDto.CategoryItemFilterRequest;
import org.example.tamaapi.dto.requestDto.MyPageRequest;
import org.example.tamaapi.dto.responseDto.MyPage;
import org.example.tamaapi.dto.responseDto.category.item.CategoryItemResponse;
import org.example.tamaapi.dto.responseDto.item.ColorItemDetailDto;
import org.example.tamaapi.dto.responseDto.ShoppingBagDto;
import org.example.tamaapi.repository.*;
import org.example.tamaapi.repository.ItemImageRepository;
import org.example.tamaapi.repository.query.*;
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
    private final ItemQueryRepository itemQueryRepository;
    private final ColorRepository colorRepository;

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

    /*
    //페이징은 페치조인 불가
    public MyPage<CategoryItemResponse> simpleCategoryItem(@RequestParam Long categoryId, @Valid MyPageRequest paginationRequest, BindingResult bindingResult) {
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
        MyPage<CategoryItemResponse> customCategoryItems = new MyPage<>(categoryItems.getContent(), categoryItems.getPageable(), categoryItems.getTotalPages());

        //자식 조회
        List<Long> itemIds = customCategoryItems.getContent().stream().map(CategoryItemResponse::getItemId).toList();
        List<RelatedColorItemQueryDto> relatedColorItemQueryDtos = colorItemQueryRepository.findAllByItemIdIn(itemIds);

        //부모에 자식 넣음
        Map<Long, List<RelatedColorItemQueryDto>> colorItemMap = relatedColorItemQueryDtos.stream()
                .collect(Collectors.groupingBy(RelatedColorItemQueryDto::getItemId));
        customCategoryItems.getContent().forEach(ci -> ci.setRelatedColorItems(colorItemMap.get(ci.getItemId())));

        return customCategoryItems;
    }


    @GetMapping("/api/items")
    public PageCustom<CategoryItemResponse> categoryItem(@RequestParam Long categoryId, @Valid PaginationRequest paginationRequest, CategoryItemFilterRequest categoryItemFilterRequest, BindingResult bindingResult) {
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

        //상위 카테고리인지 확인
        Color color = colorRepository.findWithChildrenByIdIn(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 category를 찾을 수 없습니다"));
        List<Long> categoryIds = new ArrayList<>();

        //상위 카테고리는 자식을 다 보여줌 = 전체
        if(category.getChildren().isEmpty())
            categoryIds.add(categoryId);
        else
            categoryIds.addAll(category.getChildren().stream().map(Category::getId).toList());


        List<RelatedColorItemQueryDto> relatedColorItemQueryDtos = colorItemQueryRepository.findAllByFilterAndItemIdIn
                (itemIds, categoryItemFilterRequest.getMinPrice(), categoryItemFilterRequest.getMaxPrice(), categoryItemFilterRequest.getColorIds()
                        , categoryItemFilterRequest.getGenders(), categoryItemFilterRequest.getIsContainSoldOut());

        //부모에 자식 넣음
        Map<Long, List<RelatedColorItemQueryDto>> colorItemMap = relatedColorItemQueryDtos.stream()
                .collect(Collectors.groupingBy(RelatedColorItemQueryDto::getItemId));
        customCategoryItems.getContent().forEach(ci -> ci.setRelatedColorItems(colorItemMap.get(ci.getItemId())));

        return customCategoryItems;
    }


    @GetMapping("/api/items")
    public PageCustom<ItemQueryDto> categoryItem(@RequestParam Long categoryId, @Valid PaginationRequest paginationRequest, CategoryItemFilterRequest categoryItemFilterRequest, BindingResult bindingResult) {
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


       // Page<Item> items = itemRepository.findAllByCategoryIdIn(categoryIds, pageRequest);
        Page<ItemQueryDto> items = itemQueryRepository.findAllByFilterAndItemIdIn(categoryIds, categoryItemFilterRequest.getMinPrice(), categoryItemFilterRequest.getMaxPrice(), categoryItemFilterRequest.getColorIds()
                , categoryItemFilterRequest.getGenders(), categoryItemFilterRequest.getIsContainSoldOut(), pageRequest);


        //페이징 -> 페이징 커스텀 변환
        PageCustom<ItemQueryDto> customCategoryItems = new PageCustom<>(items.getContent(), items.getPageable(), items.getTotalPages());


        return customCategoryItems;
    }
    */

    @GetMapping("/api/items")
    public List<String> categoryItem(@RequestParam Long categoryId, @Valid MyPageRequest myPageRequest, CategoryItemFilterRequest categoryItemFilterRequest, BindingResult bindingResult) {
        //상위 카테고리인지 확인
        Category category = categoryRepository.findWithChildrenById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 category를 찾을 수 없습니다"));
        List<Long> categoryIds = new ArrayList<>();

        //상위 카테고리면 자식을 다 보여줌 = 전체
        if(category.getChildren().isEmpty())
            categoryIds.add(categoryId);
        else
            categoryIds.addAll(category.getChildren().stream().map(Category::getId).toList());



        System.out.println("categoryItemFilterRequest = " + categoryItemFilterRequest.toString());

        //부모 색상과 자식 색상을 모두 보여줌
        List<Color> colors = colorRepository.findWithChildrenByIdIn(categoryItemFilterRequest.getColorIds());
        List<Long> colorIds = new ArrayList<>();
        for (Color color : colors) {
            colorIds.add(color.getId());
            colorIds.addAll(color.getChildren().stream().map(Color::getId).toList());
        }

        List<Long> itemIds = itemQueryRepository.findItemIdsByFilterAndCategoryIdIn(categoryIds, categoryItemFilterRequest.getMinPrice(), categoryItemFilterRequest.getMaxPrice(), colorIds
                , categoryItemFilterRequest.getGenders(), categoryItemFilterRequest.getIsContainSoldOut());

        PageRequest pageRequest = PageRequest.of(myPageRequest.getPage()-1, myPageRequest.getSize(), Sort.by(myPageRequest.getDirection(), myPageRequest.getProperty()));
        Page<Item> items = itemRepository.findAllByIdIn(itemIds, pageRequest);

        MyPage<Item> customCategoryItems = new MyPage<>(items.getContent(), items.getPageable(), items.getTotalPages());

        //페이징 -> 페이징 커스텀 변환
        //PageCustom<ItemQueryDto> customCategoryItems = new PageCustom<>(items.getContent(), items.getPageable(), items.getTotalPages());


        return customCategoryItems.getContent().stream().map(c-> c.getName()).toList();
    }

    @GetMapping("/test")
    public int test(){
        List<Item> test = itemRepository.test();
        return test.size();
    }



    @GetMapping("/api/items/min-max-price")
    public ItemMinMaxQueryDto categoryItemMinMaxPrice(@RequestParam Long categoryId) {
        //상위 카테고리인지 확인
        Category category = categoryRepository.findWithChildrenById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 category를 찾을 수 없습니다"));
        List<Long> categoryIds = new ArrayList<>();

        //상위 카테고리는 자식을 다 보여줌 = 전체
        if(category.getChildren().isEmpty())
            categoryIds.add(categoryId);
        else
            categoryIds.addAll(category.getChildren().stream().map(Category::getId).toList());

        return itemQueryRepository.findMinMaxPriceByCategoryIdIn(categoryIds).orElseThrow(() -> new IllegalArgumentException("해당 아이템 최소가격 최대가격을 찾을 수 없습니다"));
    }


    // 임시로 모든 아이템 로직으로 사용
    @GetMapping("/api/items/best")
    public void categoryBestItem(@RequestParam String category) {
    }


}
