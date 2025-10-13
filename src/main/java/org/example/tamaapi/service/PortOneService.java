package org.example.tamaapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.domain.order.PortOnePaymentStatus;
import org.example.tamaapi.dto.PortOneOrder;
import org.example.tamaapi.dto.requestDto.order.OrderRequest;
import org.example.tamaapi.exception.OrderFailException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.lang.reflect.Field;
import java.util.List;
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
                    //res 포함하면 복잡해서 포함 안했음. 보안상 민감한 내용 있을수도 있고
                    String clientMsg = String.format("결제 검증 중 오류가 발생했습니다. 원인: 포트원 결제내역 단건조회 API 호출 실패, 결제번호=%s", paymentId);
                    String serverMsg = String.format("결제 검증 중 오류가 발생했습니다. 원인: 포트원 결제내역 단건조회 API 호출 실패, 결제번호=%s, res=%s", paymentId, res);
                    log.error(serverMsg);
                    throw new IllegalArgumentException(clientMsg);
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
                    //res 포함하면 복잡해서 포함 안했음. 보안상 민감한 내용 있을수도 있고
                    String clientMsg = String.format("결제 취소를 실패했습니다. 원인: 포트원 결제 취소 API 호출 실패, 결제번호=%s", paymentId);
                    String serverMsg = String.format("결제 취소를 실패했습니다. 원인: 포트원 결제 취소 API 호출 실패, 결제번호=%s, res:%s", paymentId, res);
                    log.error(serverMsg);
                    throw new IllegalArgumentException(clientMsg);
                })
                .toBodilessEntity();
    }

    //프론트에서 customData 양식 못 맞추면 예외 발생 가능
    public PortOneOrder convertCustomData(String customData, String paymentId) {
        try {
            PortOneOrder portOneOrder = objectMapper.readValue(customData, PortOneOrder.class);
            validateNotBlank(portOneOrder);
            return portOneOrder;
        } catch (Exception e) {
            String cause = "PG사 데이터 읽는 중 오류 발생";
            cancelPayment(paymentId, cause);
            log.error(String.format("결제가 취소될 예정입니다. 원인:%s, customData:%s", cause, customData));
            throw new OrderFailException(cause, paymentId);
        }
    }
    /*
    //내부 리파지토리 안 쓰는 메소드라 orderService.validate와 분리
    public void validate(PortOnePaymentStatus paymentStatus, SaveOrderRequest saveOrderRequest) {
        try {
            validatePaymentStatus(paymentStatus, saveOrderRequest.getPaymentId());
        } catch (OrderFailException e) {
            log.warn(e.getMessage());
            cancelPayment(saveOrderRequest.getPaymentId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage());
            //주문 취소안하고, DB 장애 해결된 이후 관리자 페이지에서 로그 조회하여 주문 재등록하게 하는 방법도 있음
            cancelPayment(saveOrderRequest.getPaymentId(), e.getMessage());
            throw e;
        }
    }
     */

    //어짜피 결제 실패했기 때문에, 결제 취소 안해도 됨
    public void validatePaymentStatus(PortOnePaymentStatus paymentStatus, String paymentId) {
        if (!paymentStatus.equals(PortOnePaymentStatus.PAID))
            throw new OrderFailException(String.format("결제가 완료되지 않아서 주문을 진행할 수 없습니다. 결제번호:%s"), paymentId);
    }

    private void validateNotBlank(PortOneOrder portOneOrder) {
        String paymentId = portOneOrder.getPaymentId();
        // 1. SaveOrderRequest 필드 검사
        for (Field field : OrderRequest.class.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(portOneOrder);

                // String 빈 값 체크
                if (value == null || (value instanceof String && !StringUtils.hasText((String) value))) {
                    throw new OrderFailException(String.format("[%s] 값 누락", field.getName()), paymentId);
                }

                // List 내부 검사
                if (value instanceof List<?> list) {
                    for (int i = 0; i < list.size(); i++) {
                        Object element = list.get(i);
                        if (element == null) {
                            throw new OrderFailException(String.format("[%s][%d] 값 누락", field.getName(), i), paymentId);
                        }

                        // element 필드 검사 (SaveOrderItemRequest)
                        for (Field itemField : element.getClass().getDeclaredFields()) {
                            itemField.setAccessible(true);
                            Object itemValue = itemField.get(element);

                            if (itemValue == null || (itemValue instanceof String && !StringUtils.hasText((String) itemValue))) {
                                throw new OrderFailException(
                                        String.format("[%s][%d].%s 값 누락", field.getName(), i, itemField.getName()),
                                        paymentId
                                );
                            }
                        }
                    }
                }

            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }


}
