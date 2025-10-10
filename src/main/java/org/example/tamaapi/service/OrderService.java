package org.example.tamaapi.service;

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
import org.example.tamaapi.dto.requestDto.order.SaveOrderRequest;
import org.example.tamaapi.dto.requestDto.order.SaveOrderItemRequest;

import org.example.tamaapi.exception.OrderFailException;
import org.example.tamaapi.repository.JdbcTemplateRepository;
import org.example.tamaapi.repository.MemberCouponRepository;
import org.example.tamaapi.repository.MemberRepository;
import org.example.tamaapi.repository.item.ColorItemSizeStockRepository;
import org.example.tamaapi.repository.order.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.*;

import static org.example.tamaapi.util.ErrorMessageUtil.*;
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
                                Integer usedPoint,
                                List<SaveOrderItemRequest> saveOrderItemRequests) {
        try {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new OrderFailException(NOT_FOUND_MEMBER, paymentId));

            MemberCoupon memberCoupon = null;
            if (memberCouponId != 0) {
                memberCoupon = memberCouponRepository.findWithById(memberCouponId)
                        .orElseThrow(() -> new OrderFailException(NOT_FOUND_COUPON, paymentId));
                memberCoupon.changeIsUsed(true);
            }

            member.minusPoint(usedPoint);
            saveOrder(paymentId, member, null, receiverNickname, receiverPhone,
                    zipCode, streetAddress, detailAddress, message, memberCoupon, usedPoint, saveOrderItemRequests);
        } catch (OrderFailException e) {
            log.warn(e.getMessage());
            portOneService.cancelPayment(paymentId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage());
            //주문 취소안하고, DB 장애 해결되면, 관리자 페이지에서 로그 조회하여 주문 재등록하게 하는 방법도 있음
            portOneService.cancelPayment(paymentId, e.getMessage());
            throw e;
        }
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
        try {
            Guest guest = new Guest(senderNickname, senderEmail);
            return saveOrder(paymentId, null, guest, receiverNickname, receiverPhone,
                    zipCode, streetAddress, detailAddress, message, null, null, saveOrderItemRequests);
        } catch (OrderFailException e) {
            log.warn(e.getMessage());
            portOneService.cancelPayment(paymentId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage());
            //주문 취소안하고, DB 장애 해결되면, 관리자 페이지에서 로그 조회하여 주문 재등록하게 하는 방법도 있음
            portOneService.cancelPayment(paymentId, e.getMessage());
            throw e;
        }
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
                           MemberCoupon memberCoupon,
                           Integer usedPoint,
                           List<SaveOrderItemRequest> saveOrderItemRequests) {

        Delivery delivery = new Delivery(zipCode, streetAddress, detailAddress, message, receiverNickname, receiverPhone);
        List<OrderItem> orderItems = createOrderItem(saveOrderItemRequests);
        Order order = (member != null)
                ? Order.createMemberOrder(paymentId, member, delivery, memberCoupon, usedPoint, orderItems)
                : Order.createGuestOrder(paymentId, guest, delivery, orderItems);

        orderRepository.save(order);
        jdbcTemplateRepository.saveOrderItems(orderItems);
        return order.getId();
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
    private void validateOrderPrice(SaveOrderRequest saveOrderRequest, Integer clientTotal, Long memberId) {
        String paymentId = saveOrderRequest.getPaymentId();
        int orderItemsPrice = calculateOrderItemsPrice(saveOrderRequest);
        int SHIPPING_FEE = calculateShippingFee(orderItemsPrice);

        //쿠폰 검증 포함
        int priceAfterCoupon = calculatePriceAfterCoupon(saveOrderRequest, orderItemsPrice);

        Integer usedPoint = saveOrderRequest.getUsedPoint();

        //0이면 검증안해도 됨
        if (usedPoint != 0) {
            int serverPoint = memberRepository.findById(memberId)
                    .orElseThrow(() -> new OrderFailException(paymentId, NOT_FOUND_MEMBER))
                    .getPoint();
            validatePoint(usedPoint, serverPoint, priceAfterCoupon, SHIPPING_FEE, saveOrderRequest.getPaymentId());
        }

        // 무료 주문은 clientTotal이 없음. 결제를 안하기 때문
        if (clientTotal != null)
            validateFinalOrderPrice(clientTotal, priceAfterCoupon, usedPoint, SHIPPING_FEE, paymentId);
    }

    private int calculateShippingFee(int orderItemsPrice) {
        return orderItemsPrice > 40000 ? 0 : 3000;
    }

    private void validateFinalOrderPrice(int clientTotal, int priceAfterCoupon, int usedPoint, int SHIPPING_FEE, String paymentId) {
        int serverTotal = priceAfterCoupon + SHIPPING_FEE - usedPoint;

        if (clientTotal != serverTotal)
            throw new OrderFailException("클라이언트 위변조 검출", paymentId);
    }

    private void validateMemberId(Long memberId, String paymentId) {
        try {
            if (memberId == null && StringUtils.hasText(paymentId)) {
                throw new OrderFailException(
                        String.format("memberId 누락", paymentId), paymentId
                );
            }
        } catch (OrderFailException e) {
            log.warn(e.getMessage());
            portOneService.cancelPayment(paymentId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage());
            //주문 취소안하고, DB 장애 해결되면, 관리자 페이지에서 로그 조회하여 주문 재등록하게 하는 방법도 있음
            portOneService.cancelPayment(paymentId, e.getMessage());
            throw e;
        }
    }

    public void validateMemberId(Long memberId) {
        if (memberId == null)
            throw new OrderFailException("memberId 누락");
    }

    private int calculatePriceAfterCoupon(SaveOrderRequest saveOrderRequest, int orderItemsPrice) {
        Long memberCouponId = saveOrderRequest.getMemberCouponId();
        int priceAfterCoupon = orderItemsPrice;

        //쿠폰 선택안하면 기본값 0으로 옴
        if (memberCouponId != 0) {
            MemberCoupon memberCoupon = memberCouponRepository.findWithById(memberCouponId)
                    .orElseThrow(() -> new OrderFailException(NOT_FOUND_COUPON, saveOrderRequest.getPaymentId()));

            CouponType couponType = memberCoupon.getCoupon().getType();
            int discountValue = memberCoupon.getCoupon().getDiscountValue();

            priceAfterCoupon = switch (couponType) {
                case FIXED_DISCOUNT -> orderItemsPrice - discountValue;
                case PERCENT_DISCOUNT -> {
                    double discountRate = discountValue / 100.0;
                    yield (int) (orderItemsPrice * (1 - discountRate));
                }
            };
            //무료 주문은 paymentId가 null, 유료 주문은 null이면 예외 던지게 해둠
            validateCoupon(memberCoupon, priceAfterCoupon, saveOrderRequest.getPaymentId());
        }
        return priceAfterCoupon;
    }


    private int calculateOrderItemsPrice(SaveOrderRequest saveOrderRequest) {
        List<SaveOrderItemRequest> orderItems = saveOrderRequest.getOrderItems();
        List<Long> colorItemSizeStockIds = orderItems.stream().map(SaveOrderItemRequest::getColorItemSizeStockId).toList();
        List<ColorItemSizeStock> colorItemSizeStocks = colorItemSizeStockRepository.findAllWithColorItemAndItemByIdIn(colorItemSizeStockIds);

        Map<Long, Integer> idPriceMap = new HashMap<>();
        for (ColorItemSizeStock colorItemSizeStock : colorItemSizeStocks) {
            Integer nowPrice = colorItemSizeStock.getColorItem().getItem().getNowPrice();
            idPriceMap.put(colorItemSizeStock.getId(), nowPrice);
        }

        return orderItems.stream()
                .mapToInt(i -> idPriceMap.get(i.getColorItemSizeStockId()) * i.getOrderCount())
                .sum();
    }

    private void validatePoint(int usedPoint, int serverPoint, int priceAfterCoupon, int SHIPPING_FEE, String paymentId) {
        String cancelMsg = null;

        if (usedPoint > serverPoint)
            cancelMsg = "보유한 포인트보다 넘게 사용할 수 없습니다.";
        else if (usedPoint > priceAfterCoupon + SHIPPING_FEE)
            cancelMsg = "주문 가격보다 많은 포인트를 사용할 수 없습니다.";

        if (cancelMsg != null && paymentId == null)
            throw new OrderFailException(cancelMsg);

        if (cancelMsg != null)
            throw new OrderFailException(cancelMsg, paymentId);
    }

    private void validateCoupon(MemberCoupon memberCoupon, int priceAfterCoupon, String paymentId) {
        String cancelMsg = null;

        if (memberCoupon.getCoupon().getExpiresAt().isBefore(LocalDate.now()))
            cancelMsg = "쿠폰 유효기간 만료";
        else if (memberCoupon.isUsed())
            cancelMsg = "이미 사용한 쿠폰입니다.";
        else if (priceAfterCoupon < 0)
            cancelMsg = "쿠폰 금액은 주문 가격보다 넘게 사용할 수 없습니다.";

        if (cancelMsg != null && paymentId == null)
            throw new OrderFailException(cancelMsg);

        if (cancelMsg != null)
            throw new OrderFailException(cancelMsg, paymentId);
    }

    //개발 단계에서만 일어날 법한 상황인데, 출시 버전에도 필요한가?
    //-> 브라우저를 거치는 게 아니고, 포스트맨으로 요청 할 수 있어서 필요
    public void validate(SaveOrderRequest saveOrderRequest, int clientTotal, Long memberId) {
        try {
            String paymentId = saveOrderRequest.getPaymentId();
            validatePaymentId(paymentId);
            validateOrderPrice(saveOrderRequest, clientTotal, memberId);
            validateMemberId(memberId, saveOrderRequest.getPaymentId());
        } catch (OrderFailException e) {
            log.warn(e.getMessage());
            portOneService.cancelPayment(saveOrderRequest.getPaymentId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage());
            //주문 취소안하고, DB 장애 해결되면, 관리자 페이지에서 로그 조회하여 주문 재등록하게 하는 방법도 있음
            portOneService.cancelPayment(saveOrderRequest.getPaymentId(), e.getMessage());
            throw e;
        }
    }

    public void validate(SaveOrderRequest saveOrderRequest, Long memberId) {
        try {
            validateOrderPrice(saveOrderRequest, null, memberId);
            validateMemberId(memberId);
        } catch (Exception e) {
            log.warn(e.getMessage());
            throw e;
        }
    }

    private void validatePaymentId(String paymentId) {
        orderRepository.findByPaymentId(paymentId)
                .ifPresent(order -> {
                    throw new OrderFailException("이미 사용된 결제번호", paymentId);
                });
    }

    public void cancelGuestOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_ORDER));
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
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_ORDER));
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
