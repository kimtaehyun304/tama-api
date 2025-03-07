package org.example.tamaapi.dto.validator;

import lombok.RequiredArgsConstructor;
import org.example.tamaapi.domain.item.ColorItemSizeStock;
import org.example.tamaapi.dto.requestDto.order.SaveOrderItemRequest;
import org.example.tamaapi.repository.item.ColorItemSizeStockRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentValidator {

    @Value("${portOne.secret}")
    private String PORT_ONE_SECRET;

    private final ColorItemSizeStockRepository colorItemSizeStockRepository;

    //클라이언트 위변조 검증
    public void validate(String paymentId, List<SaveOrderItemRequest> saveOrderItemRequests) {
        //결제내역 단건 조회. amount.total 가져오기 위함
        Map<String, Object> paymentResponse = RestClient.create().get()
                .uri("https://api.portone.io/payments/{paymentId}", paymentId)
                .header("Authorization", "PortOne " + PORT_ONE_SECRET)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req,res) -> {throw new IllegalArgumentException("포트원 결제내역 단건조회 API 호출 실패"); })
                .body(new ParameterizedTypeReference<>() {
                });

        //클라이언트 위변조 검증
        Map<String, Object> amountMap = (Map<String, Object>) paymentResponse.get("amount");

        int clientTotal = (int) amountMap.get("total");

        List<Long> colorItemSizeStockIds = saveOrderItemRequests.stream().map(SaveOrderItemRequest::getColorItemSizeStockId).toList();
        List<ColorItemSizeStock> colorItemSizeStocks = colorItemSizeStockRepository.findAllWithColorItemAndItemByIdIn(colorItemSizeStockIds);

        Map<Long, Integer> idPriceMap = new HashMap<>();
        for (ColorItemSizeStock colorItemSizeStock : colorItemSizeStocks) {
            Integer price = colorItemSizeStock.getColorItem().getItem().getPrice();
            Integer discountedPrice = colorItemSizeStock.getColorItem().getItem().getDiscountedPrice();
            idPriceMap.put(colorItemSizeStock.getId(), discountedPrice != null ? discountedPrice : price);
        }

        int serverTotal = saveOrderItemRequests.stream().mapToInt(i -> idPriceMap.get(i.getColorItemSizeStockId()) * i.getOrderCount()).sum();

        if(clientTotal != serverTotal)
            throw new IllegalArgumentException("클라이언트 위변조 검출");

    }
}
