package org.example.tamaapi.cache;

import lombok.RequiredArgsConstructor;
import org.example.tamaapi.domain.item.Category;
import org.example.tamaapi.dto.requestDto.CustomPageRequest;
import org.example.tamaapi.repository.item.CategoryRepository;
import org.example.tamaapi.repository.item.query.ItemQueryRepository;
import org.example.tamaapi.repository.item.query.dto.CategoryBestItemQueryResponse;
import org.example.tamaapi.service.CacheService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class Scheduler {

    private final ItemQueryRepository itemQueryRepository;
    private final CategoryRepository categoryRepository;
    private final CacheService cacheService;

    @Scheduled(cron = "0 0 0 * * *")
    private void saveBestItemCache(){
        CustomPageRequest customPageRequest = new CustomPageRequest(1,10);

        //전체 인기 상품
        List<Long> emptyCategoryIds = new ArrayList<>();
        List<CategoryBestItemQueryResponse> allBestItems = itemQueryRepository.findCategoryBestItemWithPaging(emptyCategoryIds, customPageRequest);

        //아우터 인기 상품
        List<Long> outerCategoryIds = new ArrayList<>();
        Long outerCategoryId = 1L;
        Category outerCategory = categoryRepository.findWithChildrenById(outerCategoryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 category를 찾을 수 없습니다"));
        outerCategoryIds.add(outerCategoryId);
        outerCategoryIds.addAll(outerCategory.getChildren().stream().map(Category::getId).toList());
        List<CategoryBestItemQueryResponse> outerBestItems = itemQueryRepository.findCategoryBestItemWithPaging(outerCategoryIds, customPageRequest);

        //상의 인기 상품
        List<Long> topCategoryIds = new ArrayList<>();
        Long topCategoryId = 5L;
        Category topCategory = categoryRepository.findWithChildrenById(topCategoryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 category를 찾을 수 없습니다"));
        topCategoryIds.add(topCategoryId);
        topCategoryIds.addAll(topCategory.getChildren().stream().map(Category::getId).toList());
        List<CategoryBestItemQueryResponse> topBestItems = itemQueryRepository.findCategoryBestItemWithPaging(topCategoryIds, customPageRequest);

        //하의 인기 상품
        List<Long> bottomCategoryIds = new ArrayList<>();
        Long bottomCategoryId = 11L;
        Category bottomCategory = categoryRepository.findWithChildrenById(bottomCategoryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 category를 찾을 수 없습니다"));
        bottomCategoryIds.add(bottomCategoryId);
        bottomCategoryIds.addAll(bottomCategory.getChildren().stream().map(Category::getId).toList());
        List<CategoryBestItemQueryResponse> bottomBestItems = itemQueryRepository.findCategoryBestItemWithPaging(bottomCategoryIds, customPageRequest);

        //캐시 저장
        cacheService.save(MyCacheType.BEST_ITEM.getName(), String.valueOf(BestItem.ALL_BEST_ITEM), allBestItems);
        cacheService.save(MyCacheType.BEST_ITEM.getName(), String.valueOf(BestItem.OUTER_BEST_ITEM), outerBestItems);
        cacheService.save(MyCacheType.BEST_ITEM.getName(), String.valueOf(BestItem.TOP_BEST_ITEM), topBestItems);
        cacheService.save(MyCacheType.BEST_ITEM.getName(), String.valueOf(BestItem.BOTTOM_BEST_ITEM), bottomBestItems);
    }

}
