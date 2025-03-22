package org.example.tamaapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tamaapi.config.PreAuthentication;
import org.example.tamaapi.domain.item.*;
import org.example.tamaapi.dto.UploadFile;
import org.example.tamaapi.dto.requestDto.CategoryItemFilterRequest;
import org.example.tamaapi.dto.requestDto.CustomPageRequest;
import org.example.tamaapi.dto.requestDto.MySort;
import org.example.tamaapi.dto.requestDto.item.save.SaveColorItemRequest;
import org.example.tamaapi.dto.requestDto.item.save.SaveItemRequest;
import org.example.tamaapi.dto.requestDto.item.save.SaveSizeStockRequest;
import org.example.tamaapi.dto.responseDto.CustomPage;
import org.example.tamaapi.dto.responseDto.SimpleResponse;
import org.example.tamaapi.dto.responseDto.category.item.CategoryBestItemResponse;
import org.example.tamaapi.dto.responseDto.category.item.CategoryItemResponse;
import org.example.tamaapi.dto.responseDto.category.item.RelatedColorItemResponse;
import org.example.tamaapi.dto.responseDto.item.ColorItemDetailDto;
import org.example.tamaapi.dto.responseDto.ShoppingBagDto;
import org.example.tamaapi.dto.responseDto.item.RelatedColorItemDto;
import org.example.tamaapi.dto.validator.SortValidator;
import org.example.tamaapi.exception.MyBadRequestException;
import org.example.tamaapi.repository.item.*;
import org.example.tamaapi.repository.item.query.*;
import org.example.tamaapi.service.ItemService;
import org.example.tamaapi.util.FileStore;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.example.tamaapi.util.ErrorMessageUtil.*;

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
    public CustomPage<CategoryItemQueryDto> categoryItem(@RequestParam Long categoryId, @Valid CustomPageRequest customPageRequest
            , @RequestParam MySort sort, @Valid CategoryItemFilterRequest itemFilter) {

        if (itemFilter.getMinPrice() != null && itemFilter.getMaxPrice() != null && itemFilter.getMinPrice() > itemFilter.getMaxPrice())
            throw new MyBadRequestException("최소값을 최대값보다 크게 입력했습니다.");

        sortValidator.validate(sort);

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
        List<Color> colors = colorRepository.findWithChildrenByIdIn(itemFilter.getColorIds());
        for (Color color : colors) {
            colorIds.add(color.getId());
            colorIds.addAll(color.getChildren().stream().map(Color::getId).toList());
        }

        return itemQueryRepository.findCategoryItemsByFilter(sort, customPageRequest, categoryIds, itemFilter.getMinPrice(), itemFilter.getMaxPrice()
                , itemFilter.getColorIds(), itemFilter.getGenders(), itemFilter.getIsContainSoldOut());
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

        return  itemQueryRepository.findCategoryBestItemByCategoryIdIn(categoryIds, customPageRequest);
    }


    @GetMapping("/api/images/items/{storedFileName}")
    public UrlResource itemImage(@PathVariable String storedFileName) throws MalformedURLException {
        return new UrlResource("file:"+fileStore.getFullPath(storedFileName));
    }

    @PostMapping("/api/items/new")
    @PreAuthentication
    @Secured("ROLE_ADMIN")
    //이미지 파일인지 검증 필요
    public ResponseEntity<SimpleResponse> saveItems(@Valid @RequestBody SaveItemRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId()).orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_CATEGORY));

        Item item = new Item(request.getPrice(), request.getDiscountedPrice(), request.getGender(), request.getYearSeason(), request.getName(), request.getDescription()
                , request.getDateOfManufacture(), request.getCountryOfManufacture(), request.getManufacturer(), category, request.getTextile(), request.getPrecaution());


        List<Long> colorIds = request.getColorItems().stream().map(SaveColorItemRequest::getColorId).toList();

        //영속성 컨텍스트 업로드
        List<Color> colors = colorRepository.findAllById(colorIds);

        //colorItems 엔티티 생성
        List<ColorItem> colorItems = request.getColorItems().stream().map(ci -> new ColorItem(item
                , colorRepository.findById(ci.getColorId()).orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_COLOR)))).toList();


        //colorItemSizeStocks 엔티티 생성
        //key: colorId
        Map<Long, List<SaveSizeStockRequest>> sizeStockMap = request.getColorItems().stream().collect(Collectors.toMap(
                SaveColorItemRequest::getColorId,  // colorId를 Key로 사용
                SaveColorItemRequest::getSizeStocks
        ));

        List<ColorItemSizeStock> colorItemSizeStocks = colorItems.stream()
                .flatMap(ci ->
                        sizeStockMap.get(ci.getColor().getId()).stream()
                                .map(req -> new ColorItemSizeStock(ci, req.getSize(), req.getStock()))
                )
                .toList();

        //colorItemImages 엔티티 생성
        Map<Long, List<UploadFile>> uploadFileMap = request.getColorItems().stream()
                .collect(Collectors.toMap(
                        SaveColorItemRequest::getColorId,
                        ci -> {
                            List<MultipartFile> files = ci.getFiles();
                            List<UploadFile> uploadFiles = fileStore.storeFiles(files);
                            return uploadFiles;
                        }
                ));

        List<ColorItemImage> colorItemImages = colorItems.stream()
                .flatMap(ci -> {
                    List<UploadFile> uploadFiles = uploadFileMap.get(ci.getColor().getId());
                    return IntStream.range(0, uploadFiles.size())  // 인덱스를 생성
                            .mapToObj(i -> new ColorItemImage(ci, uploadFiles.get(i), i + 1)); // 1부터 시작하는 순서
                })
                .toList();

        itemService.saveItem(item, colorItems, colorItemSizeStocks, colorItemImages);

        return ResponseEntity.status(HttpStatus.OK).body(new SimpleResponse("저장 성공"));
    }


}
