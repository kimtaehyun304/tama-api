package org.example.tamaapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tamaapi.config.aspect.PreAuthentication;
import org.example.tamaapi.domain.item.*;
import org.example.tamaapi.dto.UploadFile;
import org.example.tamaapi.dto.requestDto.CategoryItemFilterRequest;
import org.example.tamaapi.dto.requestDto.CustomPageRequest;
import org.example.tamaapi.dto.requestDto.MySort;
import org.example.tamaapi.dto.requestDto.item.save.SaveColorItemRequest;
import org.example.tamaapi.dto.requestDto.item.save.SaveItemRequest;
import org.example.tamaapi.dto.requestDto.item.save.SaveSizeStockRequest;
import org.example.tamaapi.dto.responseDto.CustomPage;
import org.example.tamaapi.dto.responseDto.item.ColorItemDetailDto;
import org.example.tamaapi.dto.responseDto.ShoppingBagDto;
import org.example.tamaapi.dto.responseDto.item.RelatedColorItemDto;
import org.example.tamaapi.dto.responseDto.item.SavedColorItemIdResponse;
import org.example.tamaapi.dto.validator.SortValidator;
import org.example.tamaapi.exception.MyBadRequestException;
import org.example.tamaapi.repository.item.*;
import org.example.tamaapi.repository.item.query.*;
import org.example.tamaapi.service.ItemService;
import org.example.tamaapi.util.FileStore;
import org.springframework.core.io.UrlResource;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.util.*;
import java.util.stream.Collectors;

import static org.example.tamaapi.util.ErrorMessageUtil.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@RequiredArgsConstructor
public class ItemApiController {

    private final ColorItemRepository colorItemRepository;
    private final ColorItemImageRepository colorItemImageRepository;
    private final ColorItemSizeStockRepository colorItemSizeStockRepository;
    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final ItemQueryRepository itemQueryRepository;
    private final ColorRepository colorRepository;
    private final SortValidator sortValidator;
    private final ReviewRepository reviewRepository;
    private final ItemService itemService;
    private final FileStore fileStore;

    @GetMapping("/api/colorItems/{colorItemId}")
    public ColorItemDetailDto colorItemDetail(@PathVariable Long colorItemId) {
        ColorItem colorItem = colorItemRepository.findWithItemAndStocksByColorItemId(colorItemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 colorItem을 찾을 수 없습니다"));

        List<ColorItemImage> colorItemImage = colorItemImageRepository.findAllByColorItemId(colorItemId);

        List<ColorItem> relatedColorItems = colorItemRepository.findRelatedColorItemByItemId(colorItem.getItem().getId());
        List<Long> itemIds = relatedColorItems.stream().map(rci -> rci.getItem().getId()).toList();
        //이거 영속성 컨텍스트 충돌 날거 같은데
        List<ColorItemImage> relatedColorItemDefaultImages = colorItemImageRepository.findAllByColorItemItemIdInAndSequence(itemIds, 1);
        Map<Long, UploadFile> uploadFileMap = relatedColorItemDefaultImages.stream().collect(Collectors.toMap(ci -> ci.getColorItem().getId(), ColorItemImage::getUploadFile));
        List<RelatedColorItemDto> relatedColorItemDtos = relatedColorItems.stream().map(rci -> new RelatedColorItemDto(rci, uploadFileMap.get(rci.getId()))).toList();

        return new ColorItemDetailDto(colorItem, colorItemImage, relatedColorItemDtos);
    }

    @GetMapping("/api/colorItemSizeStock")
    public List<ShoppingBagDto> shoppingBag(@RequestParam(value = "id") List<Long> itemStockIds) {
        List<ColorItemSizeStock> colorItemSizeStocks = colorItemSizeStockRepository.findAllWithColorItemAndItemByIdIn(itemStockIds);
        List<Long> colorItemIds = colorItemSizeStocks.stream().map(ciss -> ciss.getColorItem().getId()).toList();

        //영속성 컨텍스트 저장용
        List<ColorItemImage> colorItemImages = colorItemImageRepository.findAllByColorItemIdInAndSequence(colorItemIds, 1);
        Map<Long, UploadFile> uploadFileMap = colorItemImages.stream().collect(Collectors.toMap(cii -> cii.getColorItem().getId(), ColorItemImage::getUploadFile));

        List<ShoppingBagDto> shoppingBagDtos = colorItemSizeStocks.stream()
                .map(ShoppingBagDto::new)
                .toList();

        shoppingBagDtos.forEach(sb -> sb.setUploadFile(uploadFileMap.get(sb.getColorItemId())));
        return shoppingBagDtos;
    }

    //카테고리 아이템
    //sort는 if문 검증이라 분리
    @GetMapping("/api/items")
    public CustomPage<CategoryItemQueryDto> categoryItem(@RequestParam(required = false) Long categoryId, @Valid CustomPageRequest customPageRequest
            , @RequestParam MySort sort, @Valid CategoryItemFilterRequest itemFilter) {

        if (itemFilter.getMinPrice() != null && itemFilter.getMaxPrice() != null && itemFilter.getMinPrice() > itemFilter.getMaxPrice())
            throw new MyBadRequestException("최소값을 최대값보다 크게 입력했습니다.");

        sortValidator.validate(sort);

        List<Long> categoryIds = new ArrayList<>();
        if(categoryId != null) {
            //상위 카테고리인지 확인
            Category category = categoryRepository.findWithChildrenById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 category를 찾을 수 없습니다"));
            categoryIds.add(categoryId);
            //상위 카테고리일경우 하위를 함께 보여줌
            categoryIds.addAll(category.getChildren().stream().map(Category::getId).toList());
        }

        //상위 색상일경우 하위를 함께 보여줌
        List<Long> colorIds = new ArrayList<>();
        List<Color> colors = colorRepository.findWithChildrenByIdIn(itemFilter.getColorIds());
        for (Color color : colors) {
            colorIds.add(color.getId());
            colorIds.addAll(color.getChildren().stream().map(Color::getId).toList());
        }

        return itemQueryRepository.findCategoryItemsByFilter(sort, customPageRequest, categoryIds, itemFilter.getMinPrice(), itemFilter.getMaxPrice()
                , colorIds, itemFilter.getGenders(), itemFilter.getIsContainSoldOut());
    }

    @GetMapping("/api/items/minMaxPrice")
    public ItemMinMaxQueryDto categoryItemMinMaxPrice(@RequestParam Long categoryId, @Valid CategoryItemFilterRequest itemFilter) {

        //상위 카테고리인지 확인
        Category category = categoryRepository.findWithChildrenById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 category를 찾을 수 없습니다"));
        List<Long> categoryIds = new ArrayList<>();

        //상위 카테고리일경우 하위를 함께 보여줌
        if (category.getChildren().isEmpty())
            categoryIds.add(categoryId);
        else
            categoryIds.addAll(category.getChildren().stream().map(Category::getId).toList());

        //상위 색상일경우 하위를 함께 보여줌
        List<Long> colorIds = new ArrayList<>();
        if (itemFilter != null) {
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


    @GetMapping("/api/items/best")
    public List<CategoryBestItemQueryDto> categoryBestItem(@RequestParam(required = false) Long categoryId, @ModelAttribute CustomPageRequest customPageRequest) {

        //상위 카테고리인지 확인
        List<Long> categoryIds = new ArrayList<>();
        if (categoryId != null) {
            Category category = categoryRepository.findWithChildrenById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 category를 찾을 수 없습니다"));
            //상위 카테고리일경우 하위를 함께 보여줌
            if (category.getChildren().isEmpty())
                categoryIds.add(categoryId);
            else
                categoryIds.addAll(category.getChildren().stream().map(Category::getId).toList());
        }

        return itemQueryRepository.findCategoryBestItemByCategoryIdIn(categoryIds, customPageRequest);
    }

    //S3 도입 전에 쓰던 거
    @GetMapping("/api/images/items/{storedFileName}")
    public UrlResource itemImage(@PathVariable String storedFileName) throws MalformedURLException {
        return new UrlResource("file:"+fileStore.getFullPath(storedFileName));
    }

    @PostMapping(value = "/api/items/new", consumes = {APPLICATION_JSON_VALUE, MULTIPART_FORM_DATA_VALUE})
    @PreAuthentication
    @Secured("ROLE_ADMIN")
    public SavedColorItemIdResponse saveItems(@Valid @RequestBody SaveItemRequest saveItemReq) {
        Category category = categoryRepository.findById(saveItemReq.getCategoryId()).orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_CATEGORY));

        Item item = new Item(saveItemReq.getPrice(), saveItemReq.getDiscountedPrice(), saveItemReq.getGender(), saveItemReq.getYearSeason(), saveItemReq.getName(), saveItemReq.getDescription()
                , saveItemReq.getDateOfManufacture(), saveItemReq.getCountryOfManufacture(), saveItemReq.getManufacturer(), category, saveItemReq.getTextile(), saveItemReq.getPrecaution());

        //영속성 컨텍스트 업로드
        List<Long> colorIds = saveItemReq.getColorItems().stream().map(SaveColorItemRequest::getColorId).toList();
        List<Color> colors = colorRepository.findAllById(colorIds);

        //colorItems 엔티티 생성
        List<ColorItem> colorItems = saveItemReq.getColorItems().stream().map(ci -> new ColorItem(item
                , colorRepository.findById(ci.getColorId()).orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_COLOR)))).toList();


        //colorItemSizeStocks 엔티티 생성
        //key: colorId
        Map<Long, List<SaveSizeStockRequest>> sizeStockMap = saveItemReq.getColorItems().stream().collect(Collectors.toMap(
                SaveColorItemRequest::getColorId,  // colorId를 Key로 사용
                SaveColorItemRequest::getSizeStocks
        ));

        List<ColorItemSizeStock> colorItemSizeStocks = colorItems.stream()
                .flatMap(ci ->
                        sizeStockMap.get(ci.getColor().getId()).stream()
                                .map(req -> new ColorItemSizeStock(ci, req.getSize(), req.getStock()))
                )
                .toList();

        List<Long> savedColorItemIds = itemService.saveItem(item, colorItems, colorItemSizeStocks);
        return new SavedColorItemIdResponse(savedColorItemIds);
    }


}
