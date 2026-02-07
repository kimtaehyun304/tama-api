package org.example.tamaapi;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.auth.jwt.TokenProvider;
import org.example.tamaapi.cache.MyCacheType;
import org.example.tamaapi.domain.user.Member;
import org.example.tamaapi.exception.NotEnoughStockException;
import org.example.tamaapi.repository.item.ColorItemSizeStockRepository;
import org.example.tamaapi.service.EmailService;
import org.example.tamaapi.service.ItemService;
import org.example.tamaapi.service.RedisCacheService;
import org.example.tamaapi.util.RandomStringGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
@Slf4j
class DateSqlTests {

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

    @Autowired
    private RedisCacheService redisCacheService;

    @Autowired
    private TokenProvider tokenProvider;

    //@Test
    @DisplayName("정석")
        //객체 상으론 타입이 맞으니까 실행된 듯
        //발생한 jpql: select o1_0.order_id from orders o1_0 where o1_0.updated_at>='2025-11-18T00:00:00.000+0900';
        //변환된  sql: select o1_0.order_id from orders o1_0 where o1_0.updated_at>='2025-11-19 00:00:00';
    void LocalDateTime_GOOD() {
        em.createNativeQuery(
                        "SELECT o.order_id FROM orders o WHERE o.updated_at >= now() - interval 8 day"
                )
                .getResultList();
    }


    //@Test
    @DisplayName("날짜 타입 불일치로 예외 발생해야하는데, 드라이버가 LocalDateTime을 dateTime으로 변환해줘서 됨")
        //객체 상으론 타입이 맞으니까 실행된 듯
        //발생한 jpql: select o1_0.order_id from orders o1_0 where o1_0.updated_at>='2025-11-18T00:00:00.000+0900';
        //변환된  sql: select o1_0.order_id from orders o1_0 where o1_0.updated_at>='2025-11-19 00:00:00';
    void LocalDateTime_JPQL() {
        LocalDateTime eightDaysAgo = LocalDateTime.now().minusDays(80).toLocalDate().atStartOfDay();
        em.createQuery("SELECT o.id FROM Order o WHERE o.updatedAt >= :eightDaysAgo")
                .setParameter("eightDaysAgo", eightDaysAgo)
                .getResultList();
    }


    @DisplayName("timestamp는 jqpl에서 iso 형식으로 변환된다. 하지만 드라이버가 timestamp로 변환해줘서 됨")
    //db 컬럼 타입인 datetime과 호환되는 timestamp 사용
    //발생한 jpql: SELECT o.order_id FROM orders o WHERE o.updated_at >= '2025-11-19T00:00:00.000+0900';
    //변환된  sql: SELECT o.order_id FROM orders o WHERE o.updated_at >= '2025-11-19 00:00:00'
    @Test
    void TIMESTAMP_NATIVE() {
        Timestamp eightDaysAgo = Timestamp.valueOf(LocalDateTime.now().minusDays(80).toLocalDate().atStartOfDay());
        //eightDaysAgo = 2025-11-19 00:00:00.0
        System.out.println("eightDaysAgo = " + eightDaysAgo);
        em.createNativeQuery("SELECT o.order_id FROM orders o WHERE o.updated_at >= :eightDaysAgo")
                .setParameter("eightDaysAgo", eightDaysAgo)
                .getResultList();
    }

    @DisplayName("timestamp string은 실제 쿼리와 동일")
    //발생한 jpql: SELECT o.order_id FROM orders o WHERE o.updated_at >= '2025-11-19 00:00:00.0';
    //그대로 실행: SELECT o.order_id FROM orders o WHERE o.updated_at >= '2025-11-19 00:00:00.0'
    //@Test
    void TIMESTAMP_STRING_NATIVE() {
        String eightDaysAgo = Timestamp.valueOf(LocalDateTime.now().minusDays(80).toLocalDate().atStartOfDay()).toString();
        //eightDaysAgo = 2025-11-19 00:00:00.0
        System.out.println("eightDaysAgo = " + eightDaysAgo);
        em.createNativeQuery("SELECT o.order_id FROM orders o WHERE o.updated_at >= :eightDaysAgo")
                .setParameter("eightDaysAgo", eightDaysAgo)
                .getResultList();
    }


    //@Test
    @DisplayName("날짜 타입 불일치로 예외 발생해야하는데, 근데 드라이버가 LocalDateTime을 localDate로 변환해줘서 됨")
        //네이티브 쿼리라 객체 타입 안 일치시켜도 되는데 굳이 바꾸네?
        //발생한 jpql: SELECT o.order_id FROM orders o WHERE o.updated_at >= '2025-11-19T00:00:00.000+0900'
        //변환된  sql: SELECT o.order_id FROM orders o WHERE o.updated_at >= '2025-11-19'
    void LocalDate_NATIVE() {
        LocalDate eightDaysAgo = LocalDateTime.now().minusDays(80).toLocalDate().atStartOfDay().toLocalDate();
        em.createNativeQuery("SELECT o.order_id FROM orders o WHERE o.updated_at >= :eightDaysAgo")
                .setParameter("eightDaysAgo", eightDaysAgo)
                .getResultList();
    }


    //----------예외 발생----------

    //jpql은 객체 상으로 날짜 타입이 일치해야한다.
    //@Test
    @DisplayName("IllegalArgumentException 발생")
    //Argument [2026-01-29 00:00:00.0] of type [java.sql.Timestamp] did not match parameter type [java.time.LocalDateTime (n/a)]
    void Timestamp_JPQL() {
        Timestamp eightDaysAgo = Timestamp.valueOf(LocalDateTime.now().minusDays(8).toLocalDate().atStartOfDay());
        em.createQuery("SELECT o.id FROM Order o WHERE o.updatedAt >= :eightDaysAgo")
                .setParameter("eightDaysAgo", eightDaysAgo)
                .getResultList();
    }

    //@Test
    @DisplayName("IllegalArgumentException 발생")
        //Argument [2026-01-29 00:00:00.0] of type [java.lang.String] did not match parameter type [java.time.LocalDateTime (n/a)]
    void Timestamp_String_JPQL() {
        String eightDaysAgo = Timestamp.valueOf(LocalDateTime.now().minusDays(8).toLocalDate().atStartOfDay()).toString();

        em.createQuery("SELECT o.id FROM Order o WHERE o.updatedAt >= :eightDaysAgo")
                .setParameter("eightDaysAgo", eightDaysAgo)
                .getResultList();
    }

}
