package org.example.tamaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.domain.*;
import org.example.tamaapi.domain.item.ColorItemSizeStock;
import org.example.tamaapi.dto.requestDto.order.SaveOrderItemRequest;
import org.example.tamaapi.repository.*;
import org.example.tamaapi.repository.item.ColorItemSizeStockRepository;
import org.example.tamaapi.repository.order.DeliveryRepository;
import org.example.tamaapi.repository.order.OrderItemRepository;
import org.example.tamaapi.repository.order.OrderRepository;
import org.example.tamaapi.util.ErrorMessageUtil;
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
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final OrderItemRepository orderItemRepository;
    private final ColorItemSizeStockRepository colorItemSizeStockRepository;
    private final DeliveryRepository deliveryRepository;
    private final JdbcTemplateRepository jdbcTemplateRepository;

    @Value("${portOne.secret}")
    private String PORT_ONE_SECRET;

    public void saveMemberOrder(String paymentId, Long memberId,
                                String receiverNickname,
                                String receiverPhone,
                                String zipCode,
                                String streetAddress,
                                String detailAddress,
                                String message,
                                List<SaveOrderItemRequest> saveOrderItemRequests) {

        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("등록되지 않은 회원입니다."));
        Delivery delivery = createDelivery(receiverNickname, receiverPhone, zipCode, streetAddress, detailAddress, message);
        List<OrderItem> orderItems = createOrderItem(paymentId, saveOrderItemRequests);
        Order order = Order.createMemberOrder(paymentId, member, delivery, orderItems);
        //order 저장후 orderItem 저장해야함
        orderRepository.save(order);
        jdbcTemplateRepository.saveOrderItems(orderItems);
    }


    public Long saveGuestOrder(String paymentId,
                               String senderNickname,
                               String senderEmail,
                               String senderPhone,
                                String receiverNickname,
                                String receiverPhone,
                                String zipCode,
                                String streetAddress,
                                String detailAddress,
                                String message,
                                List<SaveOrderItemRequest> saveOrderItemRequests) {

        Guest guest = new Guest(senderNickname,senderPhone,senderEmail);
        Delivery delivery = createDelivery(receiverNickname, receiverPhone, zipCode, streetAddress, detailAddress, message);
        List<OrderItem> orderItems = createOrderItem(paymentId, saveOrderItemRequests);
        Order order = Order.createGuestOrder(paymentId, guest, delivery, orderItems);
        //order 저장후 orderItem 저장해야함
        orderRepository.save(order);
        jdbcTemplateRepository.saveOrderItems(orderItems);

        return order.getId();
    }

    //saveOrder 공통 로직
    private List<OrderItem> createOrderItem(String paymentId, List<SaveOrderItemRequest> saveOrderItemRequests){
        validatePayment(paymentId, saveOrderItemRequests);
        validatePaymentId(paymentId);
        List<OrderItem> orderItems = new ArrayList<>();

        for (SaveOrderItemRequest saveOrderItemRequest : saveOrderItemRequests) {
            Long itemId = saveOrderItemRequest.getColorItemSizeStockId();
            //영속성 컨텍스트 재사용
            ColorItemSizeStock colorItemSizeStock = colorItemSizeStockRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException(itemId + "는 동록되지 않은 상품입니다"));

            //가격 변동 or 할인 쿠폰 고려
            Integer price = colorItemSizeStock.getColorItem().getItem().getPrice();
            Integer discountedPrice = colorItemSizeStock.getColorItem().getItem().getDiscountedPrice();
            int orderPrice = discountedPrice != null ? discountedPrice : price;

            OrderItem orderItem = OrderItem.builder().colorItemSizeStock(colorItemSizeStock).orderPrice(orderPrice).count(saveOrderItemRequest.getOrderCount()).build();
            orderItems.add(orderItem);
        }
        return orderItems;
    }

    //saveOrder 공통 로직
    private Delivery createDelivery(String receiverNickname,
                                    String receiverPhone,
                                    String zipCode,
                                    String streetAddress,
                                    String detailAddress,
                                    String message){
        return new Delivery(zipCode, streetAddress, detailAddress, message, receiverNickname, receiverPhone);
    }

    //클라이언트 위변조 검증
    private void validatePayment(String paymentId, List<SaveOrderItemRequest> saveOrderItemRequests) {

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

        List<Long> colorItemSizeStockIds = saveOrderItemRequests.stream().map(SaveOrderItemRequest::getColorItemSizeStockId).toList();
        List<ColorItemSizeStock> colorItemSizeStocks = colorItemSizeStockRepository.findAllWithColorItemAndItemByIdIn(colorItemSizeStockIds);

        Map<Long, Integer> idPriceMap = new HashMap<>();
        for (ColorItemSizeStock colorItemSizeStock : colorItemSizeStocks) {
            Integer price = colorItemSizeStock.getColorItem().getItem().getPrice();
            Integer discountedPrice = colorItemSizeStock.getColorItem().getItem().getDiscountedPrice();
            idPriceMap.put(colorItemSizeStock.getId(), discountedPrice != null ? discountedPrice : price);
        }

        int serverTotal = saveOrderItemRequests.stream().mapToInt(i -> idPriceMap.get(i.getColorItemSizeStockId()) * i.getOrderCount()).sum();

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
                    String format = String.format("[포트원 결제 취소 API 호출 실패] 결제번호:%s", paymentId);
                    log.info(format);
                    throw new IllegalArgumentException("포트원 결제 취소 API 호출 실패");
                })
                .toBodilessEntity();
    }

    public void cancelGuestOrder(Long orderId){
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException(ErrorMessageUtil.NOT_FOUND_ORDER));
        OrderStatus status = order.getStatus();
        if(!(status == OrderStatus.PAYMENT || status == OrderStatus.CHECK))
            throw new IllegalArgumentException("주문 취소 가능 단계가 아닙니다.");
        order.cancelOrder();
        cancelPortOnePayment(order.getPaymentId());
    }

    public void cancelMemberOrder(Long orderId, Long memberId){
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException(ErrorMessageUtil.NOT_FOUND_ORDER));

        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException(ErrorMessageUtil.NOT_FOUND_MEMBER));


        if(!member.getAuthority().equals(Authority.ADMIN) && !order.getMember().getId().equals(memberId))
            throw new IllegalArgumentException("주문한 사용자가 아닙니다.");

        OrderStatus status = order.getStatus();
        if(!(status == OrderStatus.PAYMENT || status == OrderStatus.CHECK))
            throw new IllegalArgumentException("주문 취소 가능 단계가 아닙니다.");

        order.cancelOrder();
        cancelPortOnePayment(order.getPaymentId());
    }


}
