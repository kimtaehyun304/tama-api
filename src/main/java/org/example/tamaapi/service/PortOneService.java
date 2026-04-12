package org.example.tamaapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.domain.order.Order;
import org.example.tamaapi.domain.order.OrderStatus;
import org.example.tamaapi.domain.order.PortOnePaymentStatus;
import org.example.tamaapi.dto.PortOneOrder;
import org.example.tamaapi.dto.requestDto.order.OrderRequest;
import org.example.tamaapi.exception.OrderFailException;
import org.example.tamaapi.exception.UsedPaymentIdException;
import org.example.tamaapi.exception.WillCancelPaymentException;
import org.example.tamaapi.repository.order.OrderRepository;
import org.example.tamaapi.util.ErrorMessageUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.retry.annotation.Backoff;

import org.springframework.retry.annotation.Retryable;
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
    private final OrderTxService orderTxService;
    private final OrderRepository orderRepository;

    //결제내역 단건 조회
    public Map<String, Object> findByPaymentId(String paymentId) {
        return RestClient.create().get()
                .uri("https://api.portone.io/payments/{paymentId}", paymentId)
                .header("Authorization", "PortOne " + PORT_ONE_SECRET)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    //res 포함하면 복잡해서 포함 안했음. 보안상 민감한 내용 있을수도 있고
                    String clientMsg = String.format("포트원 결제내역 단건조회 API 호출 실패");
                    String serverMsg = String.format("포트원 결제내역 단건조회 API 호출 실패, res=%s", res);
                    log.error(serverMsg);
                    throw new IllegalArgumentException(clientMsg);
                })
                .body(new ParameterizedTypeReference<>() {
                });

    }

    //@Retryable은 retey 실패가 서큣브레이커에 포함 안되서
    //retry 실패시 PG_CANCEL_ERROR상태로 변경하고 스케줄러로 나중에 재시도
    @Retry(name = "portone", fallbackMethod = "cancelRetryFallback")
    @CircuitBreaker(name = "portone", fallbackMethod = "cancelCircuitFallback")
    public void cancelPayment(String paymentId, String reason) {
        log.info("hello");
        throw new RuntimeException("에러!");
        /*
        RestClient.create().post()
                .uri("https://api.portone.io/payments/{paymentId}/cancel", paymentId)
                .header("Authorization", "PortOne " + PORT_ONE_SECRET)
                .body(Map.of("reason", reason)) // 문자열로 JSON 전달
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    //res 포함하면 복잡해서 포함 안했음. 보안상 민감한 내용 있을수도 있고
                    String clientMsg = String.format("PG 서버 문제로 인해 결제 취소를 실패했습니다");
                    String serverMsg = String.format("포트원 결제 취소 API 호출 실패로 인해 결제 취소를 실패했습니다., res:%s", res);
                    log.error(serverMsg);
                    throw new IllegalArgumentException(clientMsg);
                })
                .toBodilessEntity();
        //예외로 인해 결제가 자동 취소되는 경우가 있는데, 이 때 잘 취소됐는지 확인을 위해
        log.debug(String.format("결제가 취소됐습니다. 이유:%s, 결제번호:%s", reason, paymentId));

         */
    }


    public void cancelRetryFallback(String paymentId, String reason, Throwable t) throws Throwable {
        String errorMsg = "[retry] PG 서버 장애로 결제를 취소하지 못했습니다. 3시간 이내로 자동으로 재시도합니다";
        //log.info(errorMsg);

        System.out.println("t = " + t);
        System.out.println("t.getMessage() = " + t.getMessage());
        System.out.println("t.getCause() = " + t.getCause());
        //서큣 열렸을떄도 fallback을 retry fallback이 캐치해서 불필요한 sql 동작하는거 막으랴고
        if (t instanceof CallNotPermittedException) {
            System.out.println("if true");
            throw new IllegalArgumentException(t.getMessage());
        }

        //주문 저장 전 단계에서 실패한 경우도 있으므로 ifPresent
        orderRepository.findByPaymentId(paymentId)
                .ifPresent(order -> {
                    if(!order.getStatus().equals(OrderStatus.PG_CANCEL_ERROR))
                        orderTxService.updateOrderStatus(order.getId(), OrderStatus.PG_CANCEL_ERROR);
                });

        throw new IllegalStateException(errorMsg);
    }

    //fallback 합쳐도 되는데 서큣브레이커 잘되는지 확인하려고 분리
    //retry여도 서큿브레이커 자체가 두번 되는거라, 서큣 브레이카 fallback 두번 후, retry fallback이 캐치
    public void cancelCircuitFallback(String paymentId, String reason, Throwable t) {
        String errorMsg = "[서큣브레이커] PG 서버 장애로 결제를 취소하지 못했습니다. 3시간 이내로 자동으로 재시도합니다";
        //log.info(errorMsg);

        //서큣브레이는 정상 실행하는 경우도 자기가 일해서 자기 fallback 쓰므로
        //retry 실패후 또는 서쿳 open일때만 상태 변경 하는게 목적이라 이렇게
        if (t instanceof CallNotPermittedException) {
            // 🔥 Circuit OPEN → 즉시 실패 처리
            //log.error("[서킷 OPEN] 즉시 실패 처리");
            //주문 저장 전 단계에서 실패한 경우도 있으므로 ifPresent
            orderRepository.findByPaymentId(paymentId)
                    .ifPresent(order -> {
                        if (!order.getStatus().equals(OrderStatus.PG_CANCEL_ERROR))
                            orderTxService.updateOrderStatus(order.getId(), OrderStatus.PG_CANCEL_ERROR);
                    });
        }
    }

    //프론트에서 customData 양식 못 맞추면 예외 발생 가능
    public PortOneOrder convertCustomData(String customData, String paymentId) {

        try {
            PortOneOrder portOneOrder = objectMapper.readValue(customData, PortOneOrder.class);
            validateNotBlank(portOneOrder);
            return portOneOrder;
        } catch (JsonProcessingException e) {
            String clientMsg = "주문을 실패했습니다. 원인: PG사 데이터 직렬화 중 오류 발생";
            log.error(String.format("%s, customData:%s, message:%s", clientMsg, customData, e.getMessage()));
            cancelPayment(paymentId, clientMsg);
            throw new WillCancelPaymentException(clientMsg);
        }
        catch (Exception e) {
            String clientMsg = String.format("주문을 실패했습니다. 원인:%s", e.getMessage());
            log.error(clientMsg);
            cancelPayment(paymentId, clientMsg);
            throw new WillCancelPaymentException(clientMsg);
        }
    }

    //어짜피 결제 실패했기 때문에, 결제 취소 안해도 됨
    public void validatePaymentStatus(PortOnePaymentStatus paymentStatus) {
        if (!paymentStatus.equals(PortOnePaymentStatus.PAID))
            throw new OrderFailException("결제가 완료되지 않아서 주문을 진행할 수 없습니다");
    }

    private void validateNotBlank(PortOneOrder portOneOrder) {
        // 1. SaveOrderRequest 필드 검사
        for (Field field : PortOneOrder.class.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(portOneOrder);

                // String 빈 값 체크
                //쿠폰은 안 사용해도 정상이라 검사 제외
                if (!field.getName().equals("memberCouponId") &&  value == null || (value instanceof String && !StringUtils.hasText((String) value)))
                    throw new OrderFailException(String.format("[%s] 값 누락", field.getName()));

                // List 내부 검사
                if (value instanceof List<?> list) {
                    for (int i = 0; i < list.size(); i++) {
                        Object element = list.get(i);
                        if (element == null) {
                            throw new OrderFailException(String.format("[%s][%d] 값 누락", field.getName(), i));
                        }

                        // element 필드 검사 (SaveOrderItemRequest)
                        for (Field itemField : element.getClass().getDeclaredFields()) {
                            itemField.setAccessible(true);
                            Object itemValue = itemField.get(element);

                            if (itemValue == null || (itemValue instanceof String && !StringUtils.hasText((String) itemValue))) {
                                throw new OrderFailException(
                                        String.format("[%s][%d].%s 값 누락", field.getName(), i, itemField.getName())
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
