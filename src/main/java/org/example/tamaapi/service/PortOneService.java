package org.example.tamaapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.domain.order.PortOnePaymentStatus;
import org.example.tamaapi.dto.requestDto.order.SaveOrderRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

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
       return  RestClient.create().get()
               .uri("https://api.portone.io/payments/{paymentId}", paymentId)
               .header("Authorization", "PortOne " + PORT_ONE_SECRET)
               .retrieve()
               .onStatus(HttpStatusCode::isError, (req, res) -> {
                   throw new IllegalArgumentException("포트원 결제내역 단건조회 API 호출 실패");
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
                    String format = String.format("[%s] [포트원 결제 취소 API 호출 실패] 결제번호:%s", reason, paymentId);
                    log.error(format);
                    throw new IllegalArgumentException(format);
                })
                .toBodilessEntity();
    }

    public SaveOrderRequest extractCustomData(String customData)  {
        try {
            return objectMapper.readValue(customData, SaveOrderRequest.class);
        } catch (Exception e){
            throw new IllegalArgumentException("extractCustomData 실패");
        }
    }

    public PortOnePaymentStatus extractPaymentResult(String portOnePaymentStatus)  {
        try {
            return objectMapper.readValue(portOnePaymentStatus, PortOnePaymentStatus.class);
        } catch (Exception e){
            throw new IllegalArgumentException("extractPaymentResult 실패");
        }

    }

}
