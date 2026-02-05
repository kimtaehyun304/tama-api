package org.example.tamaapi.controller;

import org.example.tamaapi.exception.NotEnoughStockException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
// 테스트 끝나면 롤백 (auto_increment는 롤백 안됨)
@Transactional
class OrderApiControllerTest {

    //브라우저에서 클릭 방식으로만 결제 가능 -> @BeforeEach로 결제 생성 못함
    //@Test
    void saveMemberOrder() {

    }

    //@Test
    void cancelMemberOrder() {
    }

    //@Test
    void saveGuestOrder() {
    }

    //@Test
    void cancelGuestOrder() {
    }

    @Test
    void 주문_API_동시요청_RestClient() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        String url = "http://localhost:" + 5000 + "/api/orders/free/member";

        String body = """
    {
      "paymentId": "payment-1234",
      "senderNickname": "김민지",
      "senderEmail": "sender@test.com",
      "receiverNickname": "고윤하",
      "receiverPhone": "01012345678",
      "zipCode": "12345",
      "streetAddress": "서울특별시 강남구 테헤란로",
      "detailAddress": "101동 1001호",
      "deliveryMessage": "문 앞에 놓아주세요",
      "memberCouponId": 10,
      "usedPoint": 1000,
      "orderItems": [
        {"colorItemSizeStockId": 1001, "orderCount": 2},
        {"colorItemSizeStockId": 1002, "orderCount": 2}
      ]
    }
    """;

        // RestTemplate는 스레드 안전하므로 한 번만 생성해서 재사용
        var restTemplate = new org.springframework.web.client.RestTemplate();

        // 실패 결과를 수집할 스레드 안전한 컬렉션
        java.util.concurrent.ConcurrentLinkedQueue<String> failures = new java.util.concurrent.ConcurrentLinkedQueue<>();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    var headers = new org.springframework.http.HttpHeaders();
                    headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
                    headers.setBearerAuth("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJraW1hcGJlbEBnbWFpbC5jb20iLCJpYXQiOjE3NzAxNDg2NTUsImV4cCI6MTc3MDIzNTA1NSwic3ViIjoiMiJ9.6GwpBdfwMLLMtFs_CMsPQrBqk6LjWlwYXxalwr-mPKc");

                    var request = new org.springframework.http.HttpEntity<>(body, headers);

                    var resp = restTemplate.postForEntity(url, request, String.class);

                    // 상태 코드로 간단 체크: 2xx가 아니면 실패 수집
                    if (!resp.getStatusCode().is2xxSuccessful()) {
                        failures.add("Status: " + resp.getStatusCodeValue());
                    }

                } catch (Exception e) {
                    // 예외는 무시하지 말고 수집 또는 로깅
                    failures.add("Exception: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        // 모든 작업 완료 대기
        latch.await();

        // Executor 정리
        executorService.shutdown();
        if (!executorService.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
            executorService.shutdownNow();
        }

        // 실패가 있다면 테스트 실패로 처리
        if (!failures.isEmpty()) {
            // 실패 내용을 출력(디버깅용) 후 assert
            failures.forEach(System.out::println);
            org.junit.jupiter.api.Assertions.fail("동시 요청 중 일부 실패: count=" + failures.size());
        }

        // 추가로 응답 바디 검증 등 필요시 assertions 추가
    }

}