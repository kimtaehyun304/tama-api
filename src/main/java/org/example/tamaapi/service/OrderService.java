package org.example.tamaapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.domain.Authority;
import org.example.tamaapi.domain.Guest;
import org.example.tamaapi.domain.Member;
import org.example.tamaapi.domain.item.ColorItemSizeStock;
import org.example.tamaapi.domain.order.Delivery;
import org.example.tamaapi.domain.order.Order;
import org.example.tamaapi.domain.order.OrderItem;
import org.example.tamaapi.domain.order.OrderStatus;
import org.example.tamaapi.dto.requestDto.order.SaveMemberOrderRequest;
import org.example.tamaapi.dto.requestDto.order.SaveOrderItemRequest;
import org.example.tamaapi.dto.requestDto.order.SaveOrderRequest;
import org.example.tamaapi.repository.JdbcTemplateRepository;
import org.example.tamaapi.repository.MemberRepository;
import org.example.tamaapi.repository.item.ColorItemSizeStockRepository;
import org.example.tamaapi.repository.order.OrderRepository;
import org.example.tamaapi.util.ErrorMessageUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ColorItemSizeStockRepository colorItemSizeStockRepository;
    private final JdbcTemplateRepository jdbcTemplateRepository;
    private final PortOneService portOneService;
    private final ObjectMapper objectMapper;

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
                                String receiverNickname,
                                String receiverPhone,
                                String zipCode,
                                String streetAddress,
                                String detailAddress,
                                String message,
                                List<SaveOrderItemRequest> saveOrderItemRequests) {

        Guest guest = new Guest(senderNickname, senderEmail);
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
        validateTotalPrice(paymentId);
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
    private void validateTotalPrice(String paymentId) {
        Map<String, Object> paymentResponse = portOneService.findByPaymentId(paymentId);
        SaveOrderRequest saveOrderRequest = portOneService.extractCustomData((String) paymentResponse.get("customData"));
        List<SaveOrderItemRequest> orderItems = saveOrderRequest.getOrderItems();
        List<Long> colorItemSizeStockIds = orderItems.stream().map(SaveOrderItemRequest::getColorItemSizeStockId).toList();
        List<ColorItemSizeStock> colorItemSizeStocks = colorItemSizeStockRepository.findAllWithColorItemAndItemByIdIn(colorItemSizeStockIds);

        Map<Long, Integer> idPriceMap = new HashMap<>();
        for (ColorItemSizeStock colorItemSizeStock : colorItemSizeStocks) {
            Integer price = colorItemSizeStock.getColorItem().getItem().getPrice();
            Integer discountedPrice = colorItemSizeStock.getColorItem().getItem().getDiscountedPrice();
            idPriceMap.put(colorItemSizeStock.getId(), discountedPrice != null ? discountedPrice : price);
        }

        Map<String, Object> amountMap = (Map<String, Object>) paymentResponse.get("amount");
        int clientTotal = (int) amountMap.get("total");
        int serverTotal = orderItems.stream().mapToInt(i -> idPriceMap.get(i.getColorItemSizeStockId()) * i.getOrderCount()).sum();

        if (clientTotal != serverTotal) {
            portOneService.cancelPayment(paymentId, "클라이언트 위변조 검출");
            throw new IllegalArgumentException("클라이언트 위변조 검출. 결제 자동 취소");
        }

    }

    public void validateSaveOrderRequest(SaveOrderRequest saveOrderRequest){
        if(saveOrderRequest.getPaymentId() == null) {
            throw new IllegalArgumentException("paymentId 누락. 결제 취소 실패");
        }

        String paymentId = saveOrderRequest.getPaymentId();
        if (saveOrderRequest.getSenderNickname() == null) {
            String cancelMsg = "[senderNickname] 누락으로 인한 결제취소";
            portOneService.cancelPayment(paymentId, cancelMsg);
            throw new IllegalArgumentException(cancelMsg);
        }

        if (saveOrderRequest.getSenderEmail() == null) {
            String cancelMsg = "[senderEmail] 누락으로 인한 결제취소";
            portOneService.cancelPayment(paymentId, cancelMsg);
            throw new IllegalArgumentException(cancelMsg);
        }

        if (saveOrderRequest.getReceiverNickname() == null) {
            String cancelMsg = "[receiverNickname] 누락으로 인한 결제취소";
            portOneService.cancelPayment(paymentId, cancelMsg);
            throw new IllegalArgumentException(cancelMsg);
        }

        if (saveOrderRequest.getReceiverPhone() == null) {
            String cancelMsg = "[receiverPhone] 누락으로 인한 결제취소";
            portOneService.cancelPayment(paymentId, cancelMsg);
            throw new IllegalArgumentException(cancelMsg);
        }

        if (saveOrderRequest.getZipCode() == null) {
            String cancelMsg = "[zipCode] 누락으로 인한 결제취소";
            portOneService.cancelPayment(paymentId, cancelMsg);
            throw new IllegalArgumentException(cancelMsg);
        }

        if (saveOrderRequest.getStreetAddress() == null) {
            String cancelMsg = "[streetAddress] 누락으로 인한 결제취소";
            portOneService.cancelPayment(paymentId, cancelMsg);
            throw new IllegalArgumentException(cancelMsg);
        }

        if (saveOrderRequest.getDetailAddress() == null) {
            String cancelMsg = "[detailAddress] 누락으로 인한 결제취소";
            portOneService.cancelPayment(paymentId, cancelMsg);
            throw new IllegalArgumentException(cancelMsg);
        }

        if (saveOrderRequest.getDeliveryMessage() == null) {
            String cancelMsg = "[deliveryMessage] 누락으로 인한 결제취소";
            portOneService.cancelPayment(paymentId, cancelMsg);
            throw new IllegalArgumentException(cancelMsg);
        }

        if (saveOrderRequest.getOrderItems() == null || saveOrderRequest.getOrderItems().isEmpty()) {
            String cancelMsg = "[orderItems] 누락 또는 비어있음으로 인한 결제취소";
            portOneService.cancelPayment(paymentId, cancelMsg);
            throw new IllegalArgumentException(cancelMsg);
        }

        for (int i = 0; i < saveOrderRequest.getOrderItems().size(); i++) {
            SaveOrderItemRequest item = saveOrderRequest.getOrderItems().get(i);

            if (item.getColorItemSizeStockId() == null) {
                String cancelMsg = String.format("[orderItems[%d].colorItemSizeStockId] 누락으로 인한 결제취소", i);
                portOneService.cancelPayment(paymentId, cancelMsg);
                throw new IllegalArgumentException(cancelMsg);
            }

            if (item.getOrderCount() == null) {
                String cancelMsg = String.format("[orderItems[%d].orderCount] 누락으로 인한 결제취소", i);
                portOneService.cancelPayment(paymentId, cancelMsg);
                throw new IllegalArgumentException(cancelMsg);
            }
        }

    }


    private void validatePaymentId(String paymentId) {
        orderRepository.findByPaymentId(paymentId)
                .ifPresent(order -> { throw new IllegalArgumentException("이미 사용된 paymentId 입니다."); });
    }



    public void cancelGuestOrder(Long orderId, String reason){
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException(ErrorMessageUtil.NOT_FOUND_ORDER));
        OrderStatus status = order.getStatus();
        if(!(status == OrderStatus.PAYMENT || status == OrderStatus.CHECK))
            throw new IllegalArgumentException("주문 취소 가능 단계가 아닙니다.");
        order.cancelOrder();
        portOneService.cancelPayment(order.getPaymentId(), reason);
    }

    public void cancelMemberOrder(Long orderId, Long memberId, String reason){
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException(ErrorMessageUtil.NOT_FOUND_ORDER));
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException(ErrorMessageUtil.NOT_FOUND_MEMBER));

        if(!member.getAuthority().equals(Authority.ADMIN) && !order.getMember().getId().equals(memberId))
            throw new IllegalArgumentException("주문한 사용자가 아닙니다.");

        OrderStatus status = order.getStatus();
        if(!(status == OrderStatus.PAYMENT || status == OrderStatus.CHECK))
            throw new IllegalArgumentException("주문 취소 가능 단계가 아닙니다.");

        order.cancelOrder();
        portOneService.cancelPayment(order.getPaymentId(), reason);
    }


}
