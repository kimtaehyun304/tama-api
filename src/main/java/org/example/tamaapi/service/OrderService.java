package org.example.tamaapi.service;

import lombok.RequiredArgsConstructor;
import org.example.tamaapi.domain.*;
import org.example.tamaapi.domain.item.ColorItemSizeStock;
import org.example.tamaapi.dto.requestDto.order.OrderItemRequest;
import org.example.tamaapi.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final OrderItemRepository orderItemRepository;
    private final ColorItemSizeStockRepository colorItemSizeStockRepository;
    private final DeliveryRepository deliveryRepository;
    private final JdbcTemplateRepository jdbcTemplateRepository;

    @Value("${portOne.secret}")
    private String PORT_ONE_SECRET;

    //클라이언트 위변조 검증
    private void validatePayment(String paymentId, List<OrderItemRequest> orderItemRequests) {

        //결제내역 단건 조회. amount.total 가져오기 위함
        Map<String, Object> paymentResponse = RestClient.create().get()
                .uri("https://api.portone.io/payments/{paymentId}", paymentId)
                .header("Authorization", "PortOne " + PORT_ONE_SECRET)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new IllegalArgumentException("포트원 결제내역 단건조회 API 호출 실패");
                })
                .body(new ParameterizedTypeReference<>() {
                });

        Map<String, Object> amountMap = (Map<String, Object>) paymentResponse.get("amount");

        int clientTotal = (int) amountMap.get("total");

        List<Long> colorItemSizeStockIds = orderItemRequests.stream().map(OrderItemRequest::getColorItemSizeStockId).toList();
        List<ColorItemSizeStock> colorItemSizeStocks = colorItemSizeStockRepository.findAllWithColorItemAndItemByIdIn(colorItemSizeStockIds);

        Map<Long, Integer> idPriceMap = new HashMap<>();
        for (ColorItemSizeStock colorItemSizeStock : colorItemSizeStocks) {
            Integer price = colorItemSizeStock.getColorItem().getItem().getPrice();
            Integer discountedPrice = colorItemSizeStock.getColorItem().getItem().getDiscountedPrice();
            idPriceMap.put(colorItemSizeStock.getId(), discountedPrice != null ? discountedPrice : price);
        }

        int serverTotal = orderItemRequests.stream().mapToInt(i -> idPriceMap.get(i.getColorItemSizeStockId()) * i.getOrderCount()).sum();

        if (clientTotal != serverTotal) {
            cancelPortOnePayment(paymentId);
            throw new IllegalArgumentException("클라이언트 위변조 검출. 결제 자동 취소");
        }

    }

    private void validatePaymentId(String paymentId) {
        orderRepository.findByPaymentId(paymentId)
                .ifPresent(order -> { throw new IllegalArgumentException("이미 사용된 paymentId 입니다."); });
    }

    public void cancelPortOnePayment(String paymentId) {
        RestClient.create().post()
                .uri("https://api.portone.io/payments/{paymentId}/cancel", paymentId)
                .header("Authorization", "PortOne " + PORT_ONE_SECRET)
                .body(Map.of("reason", "클라이언트 위변조 검출")) // 문자열로 JSON 전달
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new IllegalArgumentException("포트원 결제 취소 API 호출 실패");
                })
                .toBodilessEntity();
    }

    public void saveMemberOrder(String paymentId, Long memberId,
                                String receiverNickname,
                                String receiverPhone,
                                String zipCode,
                                String streetAddress,
                                String detailAddress,
                                String message,
                                List<OrderItemRequest> orderItemRequests) {

        validatePayment(paymentId, orderItemRequests);
        validatePaymentId(paymentId);

        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("등록되지 않은 회원입니다."));
        Delivery delivery = new Delivery(zipCode, streetAddress, detailAddress, message, receiverNickname, receiverPhone);
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest orderItemRequest : orderItemRequests) {
            Long itemId = orderItemRequest.getColorItemSizeStockId();
            //영속성 컨텍스트 재사용
            ColorItemSizeStock colorItemSizeStock = colorItemSizeStockRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException(itemId + "는 동록되지 않은 상품입니다"));
            OrderItem orderItem = OrderItem.builder().colorItemSizeStock(colorItemSizeStock).count(orderItemRequest.getOrderCount()).build();
            orderItems.add(orderItem);
        }


        Order order = Order.createMemberOrder(paymentId, member, delivery, orderItems);
        //order 저장후 orderItem 저장해야함
        orderRepository.save(order);
        jdbcTemplateRepository.saveOrderItems(orderItems);
    }

    public void saveGuestOrder(String paymentId,
                               String senderNickname,
                               String senderEmail,
                               String senderPhone,
                                String receiverNickname,
                                String receiverPhone,
                                String zipCode,
                                String streetAddress,
                                String detailAddress,
                                String message,
                                List<OrderItemRequest> orderItemRequests) {

        validatePayment(paymentId, orderItemRequests);
        validatePaymentId(paymentId);

        Delivery delivery = new Delivery(zipCode, streetAddress, detailAddress, message, receiverNickname, receiverPhone);
        Guest guest = new Guest(senderNickname,senderPhone,senderEmail);
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest orderItemRequest : orderItemRequests) {
            Long itemId = orderItemRequest.getColorItemSizeStockId();
            //영속성 컨텍스트 재사용
            ColorItemSizeStock colorItemSizeStock = colorItemSizeStockRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException(itemId + "는 동록되지 않은 상품입니다"));
            OrderItem orderItem = OrderItem.builder().colorItemSizeStock(colorItemSizeStock).count(orderItemRequest.getOrderCount()).build();
            orderItems.add(orderItem);
        }

        Order order = Order.createGuestOrder(paymentId, guest, delivery, orderItems);
        //order 저장후 orderItem 저장해야함
        orderRepository.save(order);
        jdbcTemplateRepository.saveOrderItems(orderItems);
    }
}
