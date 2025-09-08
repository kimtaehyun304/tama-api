package org.example.tamaapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.domain.user.Authority;
import org.example.tamaapi.domain.user.Guest;
import org.example.tamaapi.domain.user.Member;
import org.example.tamaapi.domain.item.ColorItemSizeStock;
import org.example.tamaapi.domain.order.Delivery;
import org.example.tamaapi.domain.order.Order;
import org.example.tamaapi.domain.order.OrderItem;
import org.example.tamaapi.domain.order.OrderStatus;
import org.example.tamaapi.dto.requestDto.order.SaveOrderItemRequest;
import org.example.tamaapi.dto.requestDto.order.SaveOrderRequest;
import org.example.tamaapi.exception.NotEnoughStockException;
import org.example.tamaapi.repository.JdbcTemplateRepository;
import org.example.tamaapi.repository.MemberRepository;
import org.example.tamaapi.repository.item.ColorItemSizeStockRepository;
import org.example.tamaapi.repository.order.OrderRepository;
import org.example.tamaapi.util.ErrorMessageUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.*;

import static org.example.tamaapi.util.ErrorMessageUtil.NOT_FOUND_MEMBER;

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
    private final ItemService itemService;

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

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_MEMBER));

        saveOrder(paymentId, member, null, receiverNickname, receiverPhone,
                zipCode, streetAddress, detailAddress, message, saveOrderItemRequests);
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

        return saveOrder(paymentId, null, guest, receiverNickname, receiverPhone,
                zipCode, streetAddress, detailAddress, message, saveOrderItemRequests);
    }

    private Long saveOrder(String paymentId,
                           Member member,
                           Guest guest,
                           String receiverNickname,
                           String receiverPhone,
                           String zipCode,
                           String streetAddress,
                           String detailAddress,
                           String message,
                           List<SaveOrderItemRequest> saveOrderItemRequests) {

        Delivery delivery = createDelivery(receiverNickname, receiverPhone, zipCode, streetAddress, detailAddress, message);

        try {
            List<OrderItem> orderItems = createOrderItem(paymentId, saveOrderItemRequests);
            Order order = (member != null)
                    ? Order.createMemberOrder(paymentId, member, delivery, orderItems)
                    : Order.createGuestOrder(paymentId, guest, delivery, orderItems);

            orderRepository.save(order);
            jdbcTemplateRepository.saveOrderItems(orderItems);
            return order.getId();
        } catch (Exception e) {
            portOneService.cancelPayment(paymentId, e.getMessage());
            // 공통 예외 처리 & 롤백 보장
            throw e;
        }
    }

    //saveOrder 공통 로직
    private List<OrderItem> createOrderItem(String paymentId, List<SaveOrderItemRequest> saveOrderItemRequests) {
        validateTotalPrice(paymentId);
        validatePaymentId(paymentId);
        List<OrderItem> orderItems = new ArrayList<>();

        for (SaveOrderItemRequest saveOrderItemRequest : saveOrderItemRequests) {
            Long colorItemSizeStockId = saveOrderItemRequest.getColorItemSizeStockId();
            //영속성 컨텍스트 재사용
            ColorItemSizeStock colorItemSizeStock = colorItemSizeStockRepository.findById(colorItemSizeStockId)
                    .orElseThrow(() -> new IllegalArgumentException(colorItemSizeStockId + "는 동록되지 않은 상품입니다"));

            //가격 변동 or 할인 쿠폰 고려
            Integer nowPrice = colorItemSizeStock.getColorItem().getItem().getNowPrice();
            int orderPrice = nowPrice;

            OrderItem orderItem = OrderItem.builder().colorItemSizeStock(colorItemSizeStock)
                    .orderPrice(orderPrice).count(saveOrderItemRequest.getOrderCount()).build();

            itemService.removeStock(colorItemSizeStockId, saveOrderItemRequest.getOrderCount());
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
                                    String message) {
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
            Integer nowPrice = colorItemSizeStock.getColorItem().getItem().getNowPrice();
            idPriceMap.put(colorItemSizeStock.getId(), nowPrice);
        }

        Map<String, Object> amountMap = (Map<String, Object>) paymentResponse.get("amount");
        int clientTotal = (int) amountMap.get("total");
        int serverTotal = orderItems.stream().mapToInt(i -> idPriceMap.get(i.getColorItemSizeStockId()) * i.getOrderCount()).sum();

        if (clientTotal != serverTotal) {
            portOneService.cancelPayment(paymentId, "클라이언트 위변조 검출");
            String cancelMsg = "클라이언트 위변조 검출. 결제 자동 취소";
            log.error("[{}], paymentId:{}", cancelMsg, paymentId);
            throw new IllegalArgumentException(cancelMsg);
        }

    }

    //개발 단계에서만 일어날 법한 상황인데, 출시 버전에도 필요한가?
    //-> 브라우저를 거치는 게 아니고, 포스트맨으로 요청 할 수 있어서 필요
    public void validateSaveOrderRequest(SaveOrderRequest saveOrderRequest) {
        String paymentId = saveOrderRequest.getPaymentId();

        //결제는 됐는데 paymentId가 첨부 안된 경우
        if (!StringUtils.hasText(paymentId)) {
            String cancelMsg = "paymentId가 누락되어 결제를 취소할 수 없습니다. 고객센터에 문의해주세요.";
            log.error("[{}] {}", cancelMsg, saveOrderRequest);
            throw new IllegalArgumentException(cancelMsg);
        }

        for (Field field : SaveOrderRequest.class.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(saveOrderRequest);
                if (value == null || (value instanceof String && !StringUtils.hasText((String) value))) {
                    String cancelMsg = String.format("[%s] 값이 누락되어 결제를 취소합니다.", field.getName());
                    portOneService.cancelPayment(paymentId, cancelMsg);
                    throw new IllegalArgumentException(cancelMsg);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        for (int i = 0; i < saveOrderRequest.getOrderItems().size(); i++) {
            SaveOrderItemRequest item = saveOrderRequest.getOrderItems().get(i);

            if (item.getColorItemSizeStockId() == null) {
                String cancelMsg = String.format("orderItems[%d].colorItemSizeStockId 값이 누락되어 결제를 취소합니다.", i);
                portOneService.cancelPayment(paymentId, cancelMsg);
                throw new IllegalArgumentException(cancelMsg);
            }

            if (item.getOrderCount() == null) {
                String cancelMsg = String.format("orderItems[%d].orderCount 값이 누락되어 결제를 취소합니다.", i);
                portOneService.cancelPayment(paymentId, cancelMsg);
                throw new IllegalArgumentException(cancelMsg);
            }
        }

    }

    private void validatePaymentId(String paymentId) {
        orderRepository.findByPaymentId(paymentId)
                .ifPresent(order -> {
                    throw new IllegalArgumentException("이미 사용된 paymentId 입니다.");
                });
    }

    public void cancelGuestOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException(ErrorMessageUtil.NOT_FOUND_ORDER));
        OrderStatus status = order.getStatus();
        if (!(status == OrderStatus.PAYMENT || status == OrderStatus.CHECK))
            throw new IllegalArgumentException("주문 취소 가능 단계가 아닙니다.");
        order.cancelOrder();
        portOneService.cancelPayment(order.getPaymentId(), reason);
    }

    public void cancelMemberOrder(Long orderId, Long memberId, String reason) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException(ErrorMessageUtil.NOT_FOUND_ORDER));
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_MEMBER));

        if (!member.getAuthority().equals(Authority.ADMIN) && !order.getMember().getId().equals(memberId))
            throw new IllegalArgumentException("주문한 사용자가 아닙니다.");

        OrderStatus status = order.getStatus();
        if (!(status == OrderStatus.PAYMENT || status == OrderStatus.CHECK))
            throw new IllegalArgumentException("주문 취소 가능 단계가 아닙니다.");

        order.cancelOrder();
        portOneService.cancelPayment(order.getPaymentId(), reason);
    }


}
