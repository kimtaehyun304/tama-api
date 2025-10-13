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
import org.example.tamaapi.dto.PortOneOrder;
import org.example.tamaapi.dto.requestDto.order.OrderRequest;
import org.example.tamaapi.dto.requestDto.order.OrderItemRequest;

import org.example.tamaapi.exception.OrderFailException;
import org.example.tamaapi.exception.WillCancelPaymentException;
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

    private Double POINT_ACCUMULATION_RATE = 0.02;

    public void saveMemberOrder(String paymentId, Long memberId,
                                String receiverNickname,
                                String receiverPhone,
                                String zipCode,
                                String streetAddress,
                                String detailAddress,
                                String message,
                                Long memberCouponId,
                                Integer usedPoint,
                                List<OrderItemRequest> orderItems) {
        try {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new OrderFailException(NOT_FOUND_MEMBER));

            MemberCoupon memberCoupon = null;
            if (memberCouponId != 0) {
                memberCoupon = memberCouponRepository.findWithById(memberCouponId)
                        .orElseThrow(() -> new OrderFailException(NOT_FOUND_COUPON));
                memberCoupon.changeIsUsed(true);
            }

            saveOrder(paymentId, member, null, receiverNickname, receiverPhone,
                    zipCode, streetAddress, detailAddress, message, memberCouponId, usedPoint, orderItems);
            member.minusPoint(usedPoint);
            int orderItemsPrice = getOrderItemsPrice(orderItems);
            int couponPrice = getCouponPrice(memberCouponId, orderItemsPrice);
            int finalOrderPrice = orderItemsPrice - couponPrice - usedPoint;
            member.plusPoint((int) (finalOrderPrice * POINT_ACCUMULATION_RATE));
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
                               List<OrderItemRequest> orderItemRequests) {
        try {
            Guest guest = new Guest(senderNickname, senderEmail);
            return saveOrder(paymentId, null, guest, receiverNickname, receiverPhone,
                    zipCode, streetAddress, detailAddress, message, null, null, orderItemRequests);
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
                           List<OrderItemRequest> orderItemRequests) {

        Delivery delivery = new Delivery(zipCode, streetAddress, detailAddress, message, receiverNickname, receiverPhone);
        List<OrderItem> orderItems = createOrderItem(orderItemRequests);
        Order order = (member != null)
                ? Order.createMemberOrder(paymentId, member, delivery, memberCoupon, usedPoint, orderItems)
                : Order.createGuestOrder(paymentId, guest, delivery, orderItems);

        orderRepository.save(order);
        jdbcTemplateRepository.saveOrderItems(orderItems);
        return order.getId();
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

    //saveOrder 공통 로직
    private List<OrderItem> createOrderItem(List<OrderItemRequest> orderItemRequests) {
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest orderItemRequest : orderItemRequests) {
            Long colorItemSizeStockId = orderItemRequest.getColorItemSizeStockId();
            //영속성 컨텍스트 재사용
            ColorItemSizeStock colorItemSizeStock = colorItemSizeStockRepository.findById(colorItemSizeStockId)
                    .orElseThrow(() -> new IllegalArgumentException(colorItemSizeStockId + "는 동록되지 않은 상품입니다"));

            //가격 변동 or 할인 쿠폰 고려
            Integer nowPrice = colorItemSizeStock.getColorItem().getItem().getNowPrice();
            int orderPrice = nowPrice;

            OrderItem orderItem = OrderItem.builder().colorItemSizeStock(colorItemSizeStock)
                    .orderPrice(orderPrice).count(orderItemRequest.getOrderCount()).build();

            itemService.removeStock(colorItemSizeStockId, orderItemRequest.getOrderCount());
            orderItems.add(orderItem);
        }
        return orderItems;
    }

    //클라이언트 위변조 검증
    private void validateMemberOrderPrice(int orderItemsPrice, Long memberCouponId, Integer usedPoint, Integer clientTotal, Long memberId) {
        int SHIPPING_FEE = getShippingFee(orderItemsPrice);

        //쿠폰 검증 포함
        int orderPriceUsedCoupon = orderItemsPrice - getCouponPrice(memberCouponId, orderItemsPrice);
        validatePoint(usedPoint, memberId, orderPriceUsedCoupon, SHIPPING_FEE);

        int serverTotal = orderPriceUsedCoupon - usedPoint + SHIPPING_FEE;
        if (clientTotal != serverTotal)
            throw new OrderFailException("클라이언트 위변조 검출");
    }

    //게스트는 쿠폰,포인트를 못씀
    public void validateFreeOrderPrice(int orderItemsPrice, Long memberCouponId, Integer usedPoint, Long memberId) {
        int SHIPPING_FEE = getShippingFee(orderItemsPrice);


        //쿠폰 검증 포함
        int orderPriceUsedCoupon = orderItemsPrice - getCouponPrice(memberCouponId, orderItemsPrice);
        validatePoint(usedPoint, memberId, orderPriceUsedCoupon, SHIPPING_FEE);

        int serverTotal = SHIPPING_FEE + orderPriceUsedCoupon - usedPoint;

        if (serverTotal != 0)
            throw new OrderFailException("결제 금액이 0원이 아닙니다.");
    }

    private int getShippingFee(int orderItemsPrice) {
        return orderItemsPrice > 40000 ? 0 : 3000;
    }

    public void validateMemberId(Long memberId) {
        if (memberId == null)
            throw new OrderFailException("memberId 누락");
    }

    private int calculatePriceAfterCoupon(String paymentId, Long memberCouponId, int orderItemsPrice) {
        int priceAfterCoupon = orderItemsPrice;

        //쿠폰 선택안하면 기본값 0으로 옴
        if (memberCouponId != 0) {
            MemberCoupon memberCoupon = memberCouponRepository.findWithById(memberCouponId)
                    .orElseThrow(() -> new OrderFailException(NOT_FOUND_COUPON, paymentId));

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
            validateCoupon(memberCoupon, priceAfterCoupon, paymentId);
        }
        return priceAfterCoupon;
    }

    private int getCouponPrice(MemberCoupon memberCoupon, int orderItemsPrice) {

        CouponType couponType = memberCoupon.getCoupon().getType();
        int discountValue = memberCoupon.getCoupon().getDiscountValue();

        int couponPrice = switch (couponType) {
            case FIXED_DISCOUNT -> discountValue;
            case PERCENT_DISCOUNT -> (int) (orderItemsPrice * (discountValue / 100.0));
        };
        validateCoupon(memberCoupon, orderItemsPrice - couponPrice);
        return couponPrice;
    }


    public int getOrderItemsPrice(List<OrderItemRequest> orderItems) {
        List<Long> colorItemSizeStockIds = orderItems.stream().map(OrderItemRequest::getColorItemSizeStockId).toList();
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

    private void validatePoint(int usedPoint, Long memberId, int priceAfterCoupon, int SHIPPING_FEE) {
        String cancelMsg = null;

        int serverPoint = memberRepository.findById(memberId)
                .orElseThrow(() -> new OrderFailException(NOT_FOUND_MEMBER))
                .getPoint();

        if (usedPoint > serverPoint)
            cancelMsg = "보유한 포인트보다 넘게 사용할 수 없습니다.";
        else if (usedPoint > priceAfterCoupon + SHIPPING_FEE)
            cancelMsg = "주문 가격보다 많은 포인트를 사용할 수 없습니다.";

        if (cancelMsg != null)
            throw new OrderFailException(cancelMsg);
    }

    private void validateCoupon(MemberCoupon memberCoupon, int orderPriceUsedCoupon) {
        String cancelMsg = null;

        if (memberCoupon.getCoupon().getExpiresAt().isBefore(LocalDate.now()))
            cancelMsg = "쿠폰 유효기간 만료";
        else if (memberCoupon.isUsed())
            cancelMsg = "이미 사용한 쿠폰입니다.";
        else if (orderPriceUsedCoupon < 0)
            cancelMsg = "쿠폰 금액은 주문 가격보다 넘게 사용할 수 없습니다.";

        if (cancelMsg != null)
            throw new OrderFailException(cancelMsg);
    }

    //개발 단계에서만 일어날 법한 상황인데, 출시 버전에도 필요한가?
//-> 브라우저를 거치는 게 아니고, 포스트맨으로 요청 할 수 있어서 필요
    public void validateMemberOrder(PortOneOrder order, int clientTotal, Long memberId) {
        try {
            String paymentId = order.getPaymentId();
            validateMemberId(memberId);
            validatePaymentId(paymentId);
            int orderItemsPrice = getOrderItemsPrice(order.getOrderItems());
            validateMemberOrderPrice(orderItemsPrice, order.getMemberCouponId(), order.getUsedPoint(), clientTotal, memberId);
        } catch (OrderFailException e) {
            log.warn(e.getMessage());
            portOneService.cancelPayment(order.getPaymentId(), e.getMessage());
            throw new WillCancelPaymentException(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            //주문 취소안하고, DB 장애 해결되면, 관리자 페이지에서 로그 조회하여 주문 재등록하게 하는 방법도 있음
            portOneService.cancelPayment(order.getPaymentId(), e.getMessage());
            throw new WillCancelPaymentException(e.getMessage());
        }
    }

    public void validateMemberFreeOrder(OrderRequest req, Long memberId) {
        try {
            int orderItemsPrice = calculateOrderItemsPrice(req.getOrderItems());
            validateOrderPrice(orderItemsPrice, req.getMemberCouponId(), req.getUsedPoint(), , memberId);
            validateMemberId(memberId);
        } catch (Exception e) {
            log.warn(e.getMessage());
            throw e;
        }
    }

    public void validateGuestOrder(PortOneOrder order, int clientTotal, Long memberId) {
        try {
            String paymentId = order.getPaymentId();
            validateMemberId(memberId);
            validatePaymentId(paymentId);
            int orderItemsPrice = calculateOrderItemsPrice(order.getOrderItems());
            validateOrderPrice(orderItemsPrice, order.getMemberCouponId(), order.getUsedPoint(), clientTotal, paymentId, memberId);
        } catch (OrderFailException e) {
            log.warn(e.getMessage());
            portOneService.cancelPayment(order.getPaymentId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage());
            //주문 취소안하고, DB 장애 해결되면, 관리자 페이지에서 로그 조회하여 주문 재등록하게 하는 방법도 있음
            portOneService.cancelPayment(order.getPaymentId(), e.getMessage());
            throw e;
        }
    }


    private void validatePaymentId(String paymentId) {
        orderRepository.findByPaymentId(paymentId)
                .ifPresent(order -> {
                    throw new WillCancelPaymentException("이미 사용된 결제번호");
                });
    }


}
