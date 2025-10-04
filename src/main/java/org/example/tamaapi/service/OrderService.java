package org.example.tamaapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.domain.coupon.CouponType;
import org.example.tamaapi.domain.coupon.MemberCoupon;
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
import org.example.tamaapi.repository.MemberCouponRepository;
import org.example.tamaapi.repository.MemberRepository;
import org.example.tamaapi.repository.item.ColorItemSizeStockRepository;
import org.example.tamaapi.repository.order.OrderRepository;
import org.example.tamaapi.util.ErrorMessageUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.example.tamaapi.util.ErrorMessageUtil.NOT_FOUND_COUPON;
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
    private final MemberCouponRepository memberCouponRepository;

    @Value("${portOne.secret}")
    private String PORT_ONE_SECRET;

    public void saveMemberOrder(String paymentId, Long memberId,
                                String receiverNickname,
                                String receiverPhone,
                                String zipCode,
                                String streetAddress,
                                String detailAddress,
                                String message,
                                Long memberCouponId,
                                Integer point,
                                List<SaveOrderItemRequest> saveOrderItemRequests) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_MEMBER));

        MemberCoupon memberCoupon = memberCouponRepository.findWithById(memberCouponId)
                .orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_COUPON));
        memberCoupon.changeIsUsed(true);

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

        Delivery delivery = new Delivery(zipCode, streetAddress, detailAddress, message, receiverNickname, receiverPhone);

        try {
            List<OrderItem> orderItems = createOrderItem(saveOrderItemRequests);
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
    private List<OrderItem> createOrderItem(List<SaveOrderItemRequest> saveOrderItemRequests) {
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

    //클라이언트 위변조 검증
    private void validateTotalPrice(SaveOrderRequest saveOrderRequest, int clientTotal, Long memberId) {
        String paymentId = saveOrderRequest.getPaymentId();
        List<SaveOrderItemRequest> orderItems = saveOrderRequest.getOrderItems();
        List<Long> colorItemSizeStockIds = orderItems.stream().map(SaveOrderItemRequest::getColorItemSizeStockId).toList();
        List<ColorItemSizeStock> colorItemSizeStocks = colorItemSizeStockRepository.findAllWithColorItemAndItemByIdIn(colorItemSizeStockIds);

        Map<Long, Integer> idPriceMap = new HashMap<>();
        for (ColorItemSizeStock colorItemSizeStock : colorItemSizeStocks) {
            Integer nowPrice = colorItemSizeStock.getColorItem().getItem().getNowPrice();
            idPriceMap.put(colorItemSizeStock.getId(), nowPrice);
        }

        int orderItemsPrice = orderItems.stream().mapToInt(i -> idPriceMap.get(i.getColorItemSizeStockId()) * i.getOrderCount()).sum();

        MemberCoupon memberCoupon = memberCouponRepository.findWithById(saveOrderRequest.getMemberCouponId())
                .orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_COUPON));
        CouponType couponType = memberCoupon.getCoupon().getType();
        int discountValue = memberCoupon.getCoupon().getDiscountValue();

        //쿠폰 검증
        int priceAfterCoupon = switch (couponType) {
            case FIXED_DISCOUNT -> orderItemsPrice - discountValue;
            case PERCENT_DISCOUNT -> {
                double discountRate = discountValue / 100.0;
                yield (int) (orderItemsPrice * (1 - discountRate));
            }
        };
        validateCoupon(memberCoupon, priceAfterCoupon);

        //포인트 검증
        Integer appliedPoint = saveOrderRequest.getPoint();
        int serverPoint = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_MEMBER)).getPoint();
        validatePoint(appliedPoint, serverPoint);

        //주문가격 검증
        int serverTotal = priceAfterCoupon - appliedPoint;
        if (clientTotal != serverTotal) {
            portOneService.cancelPayment(paymentId, "클라이언트 위변조 검출");
            String cancelMsg = "클라이언트 위변조 검출. 결제 자동 취소";
            log.warn("[{}], paymentId:{}", cancelMsg, paymentId);
            throw new IllegalArgumentException(cancelMsg);
        }

    }

    private void validatePoint(int appliedPoint, int serverPoint) {
        if (appliedPoint > serverPoint)
            throw new IllegalArgumentException("보유한 포인트보다 넘게 사용할 수 없습니다.");

        if (appliedPoint > serverPoint)
            throw new IllegalArgumentException("주문 가격이 넘게 포인트를 사용할 수 없습니다");
    }

    private static void validateCoupon(MemberCoupon memberCoupon, Integer priceAfterCoupon) {
        if (memberCoupon.getCoupon().getExpiresAt().isBefore(LocalDate.now()))
            throw new IllegalArgumentException("쿠폰 유효기간이 만료됐습니다.");

        if (memberCoupon.isUsed())
            throw new IllegalArgumentException("이미 사용한 쿠폰입니다.");

        if (priceAfterCoupon < 0)
            throw new IllegalArgumentException("쿠폰 금액은 주문 가격보다 넘게 사용할 수 없습니다.");
    }

    //개발 단계에서만 일어날 법한 상황인데, 출시 버전에도 필요한가?
    //-> 브라우저를 거치는 게 아니고, 포스트맨으로 요청 할 수 있어서 필요
    public void validate(SaveOrderRequest saveOrderRequest, int clientTotal, Long memberId) {
        String paymentId = saveOrderRequest.getPaymentId();
        validateTotalPrice(saveOrderRequest, clientTotal, memberId);
        validatePaymentId(paymentId);
    }

    private void validatePaymentId(String paymentId) {
        orderRepository.findByPaymentId(paymentId)
                .ifPresent(order -> {
                    String message = "이미 사용된 paymentId 입니다.";
                    log.warn(message);
                    throw new IllegalArgumentException(message);
                });
    }

    public void cancelGuestOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException(ErrorMessageUtil.NOT_FOUND_ORDER));
        OrderStatus status = order.getStatus();
        if (!(status == OrderStatus.PAYMENT || status == OrderStatus.CHECK)) {
            String message = "주문 취소 가능 단계가 아닙니다.";
            log.warn(message);
            throw new IllegalArgumentException(message);
        }

        order.cancelOrder();
        portOneService.cancelPayment(order.getPaymentId(), reason);
    }

    public void cancelMemberOrder(Long orderId, Long memberId, String reason) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException(ErrorMessageUtil.NOT_FOUND_ORDER));
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_MEMBER));

        if (!member.getAuthority().equals(Authority.ADMIN) && !order.getMember().getId().equals(memberId)) {
            String message = "주문한 사용자가 아닙니다.";
            log.warn(message);
            throw new IllegalArgumentException(message);
        }

        OrderStatus status = order.getStatus();
        if (!(status == OrderStatus.PAYMENT || status == OrderStatus.CHECK)) {
            String message = "주문 취소 가능 단계가 아닙니다.";
            log.warn(message);
            throw new IllegalArgumentException(message);
        }

        order.cancelOrder();
        portOneService.cancelPayment(order.getPaymentId(), reason);
    }

}
