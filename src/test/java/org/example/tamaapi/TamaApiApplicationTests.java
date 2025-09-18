package org.example.tamaapi;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.domain.Gender;
import org.example.tamaapi.domain.item.ColorItemSizeStock;
import org.example.tamaapi.dto.requestDto.CustomPageRequest;
import org.example.tamaapi.dto.requestDto.CustomSort;
import org.example.tamaapi.exception.MyBadRequestException;
import org.example.tamaapi.exception.NotEnoughStockException;
import org.example.tamaapi.repository.item.ColorItemImageRepository;
import org.example.tamaapi.repository.item.ColorItemSizeStockRepository;
import org.example.tamaapi.repository.item.query.dto.CategoryBestItemQueryResponse;
import org.example.tamaapi.repository.item.query.dto.QCategoryItemQueryDto;
import org.example.tamaapi.service.EmailService;
import org.example.tamaapi.service.ItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.example.tamaapi.domain.item.QCategory.category;
import static org.example.tamaapi.domain.item.QColor.color;
import static org.example.tamaapi.domain.item.QColorItem.colorItem;
import static org.example.tamaapi.domain.item.QColorItemSizeStock.colorItemSizeStock;
import static org.example.tamaapi.domain.item.QItem.item;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.util.StringUtils.hasText;

@SpringBootTest
@Transactional
@Slf4j
class TamaApiApplicationTests {

    @Autowired
    private JPAQueryFactory queryFactory;

    @Autowired
    private EntityManager em;

    @Autowired
    private ItemService itemService;

    @Autowired
    private ColorItemSizeStockRepository colorItemSizeStockRepository;

    @Autowired
    private EmailService emailService;


    // 멀티쓰레드라 removeStock 테스트 롤백 안됨 -> 수동 테스트 할 것!
    public void 상품주문_동시성_문제_검증() throws InterruptedException {
        Long colorItemSizeStockId = 1L;

        // 스레드 풀 생성
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        int executeCount = 11;
        CountDownLatch countDownLatch = new CountDownLatch(executeCount);

        for (int i = 0; i < executeCount; i++) {
            executorService.submit(() -> {
                try {
                    itemService.removeStock(colorItemSizeStockId, 1);
                } catch (NotEnoughStockException e) {
                    log.error(String.valueOf(e));
                } finally {
                    // 테스트 종료를 위해 반드시 실행되야 해서 finally
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();
        int nowStock = colorItemSizeStockRepository.findById(colorItemSizeStockId).get().getStock();
        assertThat(nowStock).isEqualTo(0);
    }

    public void 이메일전송_비동기큐_예외발생로그_확인() throws InterruptedException {

        String toMailAddr = "burnaby033@naver.com";

        String buyerName = "박미숙";

        Long orderId = 40000L;

        for (int i = 0; i < 5; i++) {
            emailService.sendGuestOrderEmailAsync(toMailAddr, buyerName, orderId);
        }

        assertThat(1).isEqualTo(1);
    }

}
