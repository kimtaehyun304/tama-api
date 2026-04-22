package org.example.tamaapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.domain.order.Courier;
import org.example.tamaapi.domain.order.OrderStatus;
import org.example.tamaapi.domain.order.PortOnePaymentStatus;
import org.example.tamaapi.dto.PortOneOrder;
import org.example.tamaapi.dto.responseDto.sweetTracker.DeliveryTrackingResponse;
import org.example.tamaapi.exception.OrderFailException;
import org.example.tamaapi.exception.WillCancelPaymentException;
import org.example.tamaapi.repository.order.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class SweetTrackerService {

    @Value("${sweetTracker.apiKey}")
    private String apiKey;


    @CircuitBreaker(name="sweetTracker")
    public DeliveryTrackingResponse findTrackingInfo(String courier, String trackingNumber) {
        String tCode = Courier.valueOf(courier).getCode();
        DeliveryTrackingResponse response = RestClient.create().get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("info.sweettracker.co.kr")
                        .path("/api/v1/trackingInfo")
                        .queryParam("t_code", tCode)
                        .queryParam("t_invoice", trackingNumber)
                        .queryParam("t_key", apiKey)
                        .build()
                )
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    String body = "";
                    try {
                        body = new String(res.getBody().readAllBytes());
                    } catch (Exception e) {
                        log.error("응답 바디 읽기 실패", e);
                    }

                    String clientMsg = "스윗트래커 택배 조회 API 호출 실패";
                    String serverMsg = String.format("스윗트래커 택배 조회 API 호출 실패, body=%s", body);
                    log.error(serverMsg);
                    throw new IllegalArgumentException(clientMsg);
                })
                .body(DeliveryTrackingResponse.class);

        if(ObjectUtils.isEmpty(response.getTrackingDetails()))
            throw new IllegalArgumentException("배송 정보가 없습니다");

        String kor = Courier.valueOf(courier).getKor();
        response.setCourierName(kor);
        return response;
    }

}
