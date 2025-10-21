package org.example.tamaapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.cache.BestItem;
import org.example.tamaapi.cache.MyCacheType;
import org.example.tamaapi.domain.item.Category;
import org.example.tamaapi.domain.order.Order;
import org.example.tamaapi.domain.order.OrderStatus;
import org.example.tamaapi.dto.requestDto.CustomPageRequest;
import org.example.tamaapi.repository.item.CategoryRepository;
import org.example.tamaapi.repository.item.query.ItemQueryRepository;
import org.example.tamaapi.repository.item.query.dto.CategoryBestItemQueryResponse;
import org.example.tamaapi.repository.order.OrderRepository;
import org.example.tamaapi.service.CacheService;
import org.example.tamaapi.service.OrderService;
import org.example.tamaapi.util.ErrorMessageUtil;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class Scheduler {

    private final ItemQueryRepository itemQueryRepository;
    private final CategoryRepository categoryRepository;
    private final CacheService cacheService;
    private final JobLauncher jobLauncher;
    private final Job completeOrderJob;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
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

    //사용하지 않는 이미지를 주기적으로 제거하려했는데, 이미지 수정할 때 비동기로 지워주면 됨!

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    //@EventListener(ApplicationReadyEvent.class)
    public void runCompleteOrderJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLocalDate("time", LocalDate.now())
                    .toJobParameters();
            jobLauncher.run(completeOrderJob, jobParameters);
        } catch (Exception e) {
            log.error("자동 구매확정 배치 실행 실패: " + e.getMessage());
        }
    }

}
