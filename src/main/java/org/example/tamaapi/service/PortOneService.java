package org.example.tamaapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.domain.order.PortOnePaymentStatus;
import org.example.tamaapi.dto.requestDto.order.SaveOrderItemRequest;
import org.example.tamaapi.dto.requestDto.order.SaveOrderRequest;
import org.example.tamaapi.exception.OrderFailException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.lang.reflect.Field;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class PortOneService {

    @Value("${portOne.secret}")
    private String PORT_ONE_SECRET;
    private final ObjectMapper objectMapper;


    //결제내역 단건 조회
    public Map<String, Object> findByPaymentId(String paymentId) {
        return RestClient.create().get()
                .uri("https://api.portone.io/payments/{paymentId}", paymentId)
                .header("Authorization", "PortOne " + PORT_ONE_SECRET)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    String format = String.format("포트원 결제내역 단건조회 API 호출 실패 / paymentId=%s, res=%s", paymentId, res);
                    log.error(format);
                    throw new IllegalArgumentException(format);
                })
                .body(new ParameterizedTypeReference<>() {
                });

    }

    public void cancelPayment(String paymentId, String reason) {
        RestClient.create().post()
                .uri("https://api.portone.io/payments/{paymentId}/cancel", paymentId)
                .header("Authorization", "PortOne " + PORT_ONE_SECRET)
                .body(Map.of("reason", reason)) // 문자열로 JSON 전달
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    String format = String.format("포트원 결제 취소 API 호출 실패 / paymentId=%s, res=%s", paymentId, res);
                    log.error(format);
                    throw new IllegalArgumentException(format);
                })
                .toBodilessEntity();
    }

    public SaveOrderRequest convertCustomData(String customData) {
        try {
            return objectMapper.readValue(customData, SaveOrderRequest.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("extractCustomData 실패");
        }
    }

    //내부 리파지토리 안 쓰는 메소드라 orderService 에 안 넣음
    public void validate(PortOnePaymentStatus paymentStatus, SaveOrderRequest saveOrderRequest) {
        validateIsEmpty(saveOrderRequest);
        validatePaymentStatus(paymentStatus);
    }

    private void validateIsEmpty(SaveOrderRequest saveOrderRequest) {
        String paymentId = saveOrderRequest.getPaymentId();
        //결제는 됐는데 paymentId가 첨부 안된 경우
        if (!StringUtils.hasText(paymentId)) {
            String cancelMsg = "paymentId가 누락되어 결제를 취소할 수 없습니다. 고객센터에 문의해주세요.";
            log.error("[{}] {}", cancelMsg, saveOrderRequest);
            throw new IllegalArgumentException(cancelMsg);
        }

        //리플렉션으로 클래스 필드값 가져옴
        for (Field field : SaveOrderRequest.class.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(saveOrderRequest);
                if (value == null || (value instanceof String && !StringUtils.hasText((String) value))) {
                    String cancelMsg = String.format("[%s] 값 누락", field.getName());
                    cancelPayment(paymentId, cancelMsg);
                    throw new OrderFailException(cancelMsg);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        for (int i = 0; i < saveOrderRequest.getOrderItems().size(); i++) {
            SaveOrderItemRequest item = saveOrderRequest.getOrderItems().get(i);

            if (item.getColorItemSizeStockId() == null) {
                String cancelMsg = String.format("orderItems[%d].colorItemSizeStockId 값 누락", i);
                cancelPayment(paymentId, cancelMsg);
                throw new OrderFailException(cancelMsg);
            }

            if (item.getOrderCount() == null) {
                String cancelMsg = String.format("orderItems[%d].orderCount 값 누락", i);
                cancelPayment(paymentId, cancelMsg);
                throw new OrderFailException(cancelMsg);
            }
        }
    }

    //어짜피 결제 실패했기 때문에, 결제 취소 안해도 됨
    private void validatePaymentStatus(PortOnePaymentStatus paymentStatus) {
        if (!paymentStatus.equals(PortOnePaymentStatus.PAID))
            throw new IllegalArgumentException("포트원 결제 실패로 인한 주문 진행 불가");
    }

}
