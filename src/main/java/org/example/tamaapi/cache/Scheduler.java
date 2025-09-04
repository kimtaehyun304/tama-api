package org.example.tamaapi.cache;

import lombok.RequiredArgsConstructor;
import org.example.tamaapi.domain.item.Category;
import org.example.tamaapi.dto.requestDto.CustomPageRequest;
import org.example.tamaapi.repository.item.CategoryRepository;
import org.example.tamaapi.repository.item.query.ItemQueryRepository;
import org.example.tamaapi.repository.item.query.dto.CategoryBestItemQueryResponse;
import org.example.tamaapi.service.CacheService;
import org.example.tamaapi.util.ErrorMessageUtil;
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

        // 전체, 아우터, 상의, 하의 총 4개 경우 캐시 저장
        for (BestItem bestItem : BestItem.values()) {
            List<Long> categoryIds = new ArrayList<>();

            if(bestItem.getCategoryId() != null){
                Category category = categoryRepository.findWithChildrenById(bestItem.getCategoryId())
                        .orElseThrow(() -> new IllegalArgumentException(ErrorMessageUtil.NOT_FOUND_CATEGORY));
                categoryIds.add(bestItem.getCategoryId());
                categoryIds.addAll(category.getChildren().stream().map(Category::getId).toList());
            }

            // 캐시 저장
            List<CategoryBestItemQueryResponse> bestItems = itemQueryRepository.findCategoryBestItemWithPaging(categoryIds, customPageRequest);
            cacheService.save(MyCacheType.BEST_ITEM, bestItem.name(), bestItems);
        }

    }

}
