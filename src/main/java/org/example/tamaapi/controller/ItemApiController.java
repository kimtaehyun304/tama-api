package org.example.tamaapi.controller;

import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tamaapi.cache.BestItem;
import org.example.tamaapi.cache.MyCacheType;
import org.example.tamaapi.aspect.PreAuthentication;
import org.example.tamaapi.domain.item.*;
import org.example.tamaapi.dto.UploadFile;
import org.example.tamaapi.dto.requestDto.CategoryItemFilterRequest;
import org.example.tamaapi.dto.requestDto.CustomPageRequest;
import org.example.tamaapi.dto.requestDto.CustomSort;
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
import org.example.tamaapi.repository.item.query.dto.CategoryBestItemQueryResponse;
import org.example.tamaapi.repository.item.query.dto.CategoryItemQueryDto;
import org.example.tamaapi.service.CacheService;
import org.example.tamaapi.service.ItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
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
    private final CategoryRepository categoryRepository;
    private final ItemQueryRepository itemQueryRepository;
    private final ColorRepository colorRepository;
    private final SortValidator sortValidator;
    private final ItemService itemService;
    private final CacheService cacheService;
    private final ItemRepository itemRepository;
    private final EntityManager em;

    @GetMapping("/api/colorItems/{colorItemId}")
    //select 필드 너무 많아서 dto 조회 개선 필요
    public ColorItemDetailDto colorItemDetail(@PathVariable Long colorItemId) {
        ColorItem colorItem = colorItemRepository.findWithItemAndStocksByColorItemId(colorItemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 colorItem을 찾을 수 없습니다"));

        //해당 상품 모든 이미지
        List<ColorItemImage> colorItemImage = colorItemImageRepository.findAllByColorItemId(colorItemId);

        //연관 상품 썸네일들
        List<ColorItem> relatedColorItems = colorItemRepository.findRelatedColorItemByItemId(colorItem.getItem().getId());
        List<Long> colorItemIds = relatedColorItems.stream().map(ColorItem::getId).toList();

        //이거 영속성 컨텍스트 충돌 날거 같은데 (충돌 안남)
        List<ColorItemImage> relatedColorItemDefaultImages = colorItemImageRepository.findAllByColorItemIdInAndSequence(colorItemIds, 1);
        Map<Long, UploadFile> uploadFileMap = relatedColorItemDefaultImages.stream().collect(Collectors.toMap(ci -> ci.getColorItem().getId(), ColorItemImage::getUploadFile));
        List<RelatedColorItemDto> relatedColorItemDtos = relatedColorItems.stream().map(rci -> new RelatedColorItemDto(rci, uploadFileMap.get(rci.getId()))).toList();

        return new ColorItemDetailDto(colorItem, colorItemImage, relatedColorItemDtos);
    }

    @GetMapping("/api/colorItemSizeStock")
    public List<ShoppingBagDto> shoppingBag(@RequestParam(value = "id") List<Long> itemStockIds) {
        List<ColorItemSizeStock> colorItemSizeStocks = colorItemSizeStockRepository.findAllWithColorItemAndItemByIdIn(itemStockIds);
        List<Long> colorItemIds = colorItemSizeStocks.stream().map(ciss -> ciss.getColorItem().getId()).toList();

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
            , @RequestParam CustomSort sort, @Valid CategoryItemFilterRequest itemFilter) {

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
        if(!CollectionUtils.isEmpty(itemFilter.getColorIds())) {
            List<Color> colors = colorRepository.findWithChildrenByIdIn(itemFilter.getColorIds());
            for (Color color : colors) {
                colorIds.add(color.getId());
                colorIds.addAll(color.getChildren().stream().map(Color::getId).toList());
            }
        }

        return itemQueryRepository.findCategoryItemsWithPagingAndSort(sort, customPageRequest, categoryIds, itemFilter.getItemName(), itemFilter.getMinPrice()
                , itemFilter.getMaxPrice(), colorIds, itemFilter.getGenders(), itemFilter.getIsContainSoldOut());
    }

    @GetMapping("/api/items/best")
    public List<CategoryBestItemQueryResponse> categoryBestItem(@RequestParam(required = false) Long categoryId, @ModelAttribute CustomPageRequest customPageRequest) {
        BestItem bestItem = BestItem.ALL_BEST_ITEM;

        List<Long> categoryIds = new ArrayList<>();
        if (categoryId != null) {
            Category category = categoryRepository.findWithChildrenById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_CATEGORY));
            categoryIds.add(categoryId);
            //상위 카테고리의 자식 포함
            categoryIds.addAll(category.getChildren().stream().map(Category::getId).toList());

            //카테고리 분류
             bestItem = switch (categoryId.intValue()){
                case 1 -> BestItem.OUTER_BEST_ITEM;
                case 5 -> BestItem.TOP_BEST_ITEM;
                case 11 -> BestItem.BOTTOM_BEST_ITEM;
                default -> throw new IllegalStateException("카테고리는 전체, 아우터, 상의, 하의 중 하나만 제공됩니다.");
             };
        }

        return (List<CategoryBestItemQueryResponse>) cacheService.get(MyCacheType.BEST_ITEM, bestItem.name());
    }

    //S3 도입 전에 쓰던 거
    /*
    @GetMapping("/api/images/items/{storedFileName}")
    public UrlResource itemImage(@PathVariable String storedFileName) throws MalformedURLException {
        return new UrlResource("file:"+fileStore.getFullPath(storedFileName));
    }
    */

    @PostMapping(value = "/api/items/new", consumes = {APPLICATION_JSON_VALUE, MULTIPART_FORM_DATA_VALUE})
    @PreAuthentication
    @Secured("ROLE_ADMIN")
    public ResponseEntity<SavedColorItemIdResponse> saveItems(@Valid @RequestBody SaveItemRequest req) {
        Category category = categoryRepository.findById(req.getCategoryId()).orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_CATEGORY));

        Item item = new Item(req.getOriginalPrice(), req.getNowPrice(), req.getGender(),
                req.getYearSeason(), req.getName(), req.getDescription()
                , req.getDateOfManufacture(), req.getCountryOfManufacture(),
                req.getManufacturer(), category, req.getTextile(), req.getPrecaution());

        //영속성 컨텍스트 등록
        List<Long> colorIds = req.getColorItems().stream().map(SaveColorItemRequest::getColorId).toList();
        List<Color> colors = colorRepository.findAllById(colorIds);

        //colorItems 엔티티 생성
        //영속성 컨텍스트에서 꺼냄
        List<ColorItem> colorItems = req.getColorItems().stream().map(ci ->
                new ColorItem(item
                , colorRepository.findById(ci.getColorId())
                        .orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_COLOR))
                )
        ).toList();

        //colorItemSizeStocks 엔티티 생성
        //key: colorId
        Map<Long, List<SaveSizeStockRequest>> sizeStockMap = req.getColorItems().stream().collect(Collectors.toMap(
                SaveColorItemRequest::getColorId,  // colorId를 Key로 사용
                SaveColorItemRequest::getSizeStocks
        ));

        List<ColorItemSizeStock> colorItemSizeStocks = colorItems.stream()
                .flatMap(ci ->
                        sizeStockMap.get(ci.getColor().getId())
                                .stream()
                                .map(request -> new ColorItemSizeStock(ci, request.getSize(), request.getStock()))
                )
                .toList();

        List<Long> savedColorItemIds = itemService.saveItem(item, colorItems, colorItemSizeStocks);

        return ResponseEntity.status(HttpStatus.CREATED).body(new SavedColorItemIdResponse(savedColorItemIds));
    }

}
