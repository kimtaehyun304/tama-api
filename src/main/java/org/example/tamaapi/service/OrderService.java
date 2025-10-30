package org.example.tamaapi.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.domain.user.coupon.CouponType;
import org.example.tamaapi.domain.user.coupon.MemberCoupon;
import org.example.tamaapi.domain.user.Authority;
import org.example.tamaapi.domain.user.Guest;
import org.example.tamaapi.domain.user.Member;
import org.example.tamaapi.domain.item.ColorItemSizeStock;
import org.example.tamaapi.domain.order.Delivery;
import org.example.tamaapi.domain.order.Order;
import org.example.tamaapi.domain.order.OrderItem;
import org.example.tamaapi.domain.order.OrderStatus;
import org.example.tamaapi.dto.PortOneOrder;
import org.example.tamaapi.dto.requestDto.order.OrderItemRequest;

import org.example.tamaapi.exception.UsedPaymentIdException;
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
    private final EntityManager em;

    @Value("${portOne.secret}")
    private String PORT_ONE_SECRET;

    private Double POINT_ACCUMULATION_RATE = 0.005;

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

            //주문 후에, 쿠폰 처리하는게 이상적이자만, saveOrder에 memberCoupon 넘겨야해서 미리 처리함
            //주문 예외나면 쿠폰 롤백되서 미리 처리해도 괜찮음
            if (memberCouponId != null) {
                memberCoupon = memberCouponRepository.findById(memberCouponId)
                        .orElseThrow(() -> new OrderFailException(NOT_FOUND_COUPON));
                memberCoupon.changeIsUsed(true);
            }
            //사용한 포인트 차감
            member.minusPoint(usedPoint);

            saveOrder(paymentId, member, null, receiverNickname, receiverPhone,
                    zipCode, streetAddress, detailAddress, message, memberCoupon, usedPoint, orderItems);

            //포인트 적립
            int orderItemsPrice = getOrderItemsPrice(orderItems);
            int accumulatedPoint = (int) ((orderItemsPrice - getCouponPrice(memberCoupon, orderItemsPrice) - usedPoint) * POINT_ACCUMULATION_RATE);
            member.plusPoint(accumulatedPoint);
        } catch (OrderFailException e) {
            log.warn(e.getMessage());
            portOneService.cancelPayment(paymentId, e.getMessage());
            throw new WillCancelPaymentException(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            //주문 취소안하고, DB 장애 해결되면, 관리자 페이지에서 로그 조회하여 주문 재등록하게 하는 방법도 있음
            portOneService.cancelPayment(paymentId, e.getMessage());
            throw new WillCancelPaymentException(e.getMessage());
        }
    }

    public void saveMemberFreeOrder(Long memberId,
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

            //주문 후에, 쿠폰 처리하는게 이상적이자만, saveOrder에 memberCoupon 넘겨야해서 미리 처리함
            //주문 예외나면 쿠폰 롤백되서 미리 처리해도 괜찮음
            if (memberCouponId != null) {
                memberCoupon = memberCouponRepository.findById(memberCouponId)
                        .orElseThrow(() -> new OrderFailException(NOT_FOUND_COUPON));
                memberCoupon.changeIsUsed(true);
            }
            //사용한 포인트 차감
            member.minusPoint(usedPoint);

            saveOrder(null, member, null, receiverNickname, receiverPhone,
                    zipCode, streetAddress, detailAddress, message, memberCoupon, usedPoint, orderItems);

            //무료 주문이라 포인트 적립 없음
        } catch (OrderFailException e) {
            log.warn(e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage());
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
                               List<OrderItemRequest> orderItems) {
        try {
            Guest guest = new Guest(senderNickname, senderEmail);
            return saveOrder(paymentId, null, guest, receiverNickname, receiverPhone,
                    zipCode, streetAddress, detailAddress, message, null, 0, orderItems);
        } catch (OrderFailException e) {
            log.warn(e.getMessage());
            portOneService.cancelPayment(paymentId, e.getMessage());
            throw new WillCancelPaymentException(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            //주문 취소안하고, DB 장애 해결되면, 관리자 페이지에서 로그 조회하여 주문 재등록하게 하는 방법도 있음
            portOneService.cancelPayment(paymentId, e.getMessage());
            throw new WillCancelPaymentException(e.getMessage());
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
                           int usedPoint,
                           List<OrderItemRequest> orderItemRequests) {

        int orderItemsPrice = getOrderItemsPrice(orderItemRequests);
        int usedCouponPrice = getCouponPrice(memberCoupon, orderItemsPrice);
        Delivery delivery = new Delivery(zipCode, streetAddress, detailAddress, message, receiverNickname, receiverPhone);
        List<OrderItem> orderItems = createOrderItem(orderItemRequests);
        Order order = (member != null)
                ? Order.createMemberOrder(paymentId, member, delivery, memberCoupon, usedCouponPrice, usedPoint, getShippingFee(orderItemsPrice), orderItems)
                : Order.createGuestOrder(paymentId, guest, delivery, getShippingFee(orderItemsPrice), orderItems);

        orderRepository.save(order);
        jdbcTemplateRepository.saveOrderItems(orderItems);
        return order.getId();
    }

    public void cancelGuestOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_ORDER));
        OrderStatus status = order.getStatus();
        //나머지 케이스는 취소 불가
        if (!(status == OrderStatus.ORDER_RECEIVED || status == OrderStatus.DELIVERED)) {
            String message = "주문 취소 가능 단계가 아닙니다.";
            log.warn(message);
            throw new IllegalArgumentException(message);
        }

        order.cancelOrder();
        portOneService.cancelPayment(order.getPaymentId(), reason);
    }

    public void cancelMemberOrder(Long orderId, Long memberId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_ORDER));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_MEMBER));

        if (!member.getAuthority().equals(Authority.ADMIN) && !order.getMember().getId().equals(memberId)) {
            String message = "주문한 사용자가 아닙니다.";
            log.warn(message);
            throw new IllegalArgumentException(message);
        }

        OrderStatus status = order.getStatus();
        //나머지 케이스는 취소 불가 (운영자여도 마찬가지)
        if (!(status == OrderStatus.ORDER_RECEIVED || status == OrderStatus.DELIVERED)) {
            String message = "주문 취소 가능 단계가 아닙니다.";
            log.warn(message);
            throw new IllegalArgumentException(message);
        }

        order.cancelOrder();
        portOneService.cancelPayment(order.getPaymentId(), reason);
    }

    public void cancelMemberFreeOrder(Long orderId, Long memberId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_ORDER));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_MEMBER));

        if (!member.getAuthority().equals(Authority.ADMIN) && !order.getMember().getId().equals(memberId)) {
            String message = "주문한 사용자가 아닙니다.";
            log.warn(message);
            throw new IllegalArgumentException(message);
        }

        OrderStatus status = order.getStatus();
        //나머지 케이스는 취소 불가 (운영자여도 마찬가지)
        if (!(status == OrderStatus.ORDER_RECEIVED || status == OrderStatus.DELIVERED)) {
            String message = "주문 취소 가능 단계가 아닙니다.";
            log.warn(message);
            throw new IllegalArgumentException(message);
        }

        order.cancelOrder();
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

    public int getCouponPrice(MemberCoupon memberCoupon, int orderItemsPrice) {
        if (memberCoupon == null) return 0;

        CouponType couponType = memberCoupon.getCoupon().getType();
        int discountValue = memberCoupon.getCoupon().getDiscountValue();

        return switch (couponType) {
            case FIXED_DISCOUNT -> discountValue;
            case PERCENT_DISCOUNT -> (int) Math.round(orderItemsPrice * (discountValue / 100.0));
        };
    }

    private void validateCoupon(MemberCoupon memberCoupon, int orderItemsPrice) {
        String cancelMsg = null;
        int couponPrice = getCouponPrice(memberCoupon, orderItemsPrice);

        if (memberCoupon.getCoupon().getExpiresAt().isBefore(LocalDate.now()))
            cancelMsg = "쿠폰 유효기간 만료";
        else if (memberCoupon.isUsed())
            cancelMsg = "이미 사용한 쿠폰입니다.";
        else if (couponPrice > orderItemsPrice)
            cancelMsg = "쿠폰 금액은 주문 가격보다 넘게 사용할 수 없습니다.";

        if (cancelMsg != null)
            throw new OrderFailException(cancelMsg);
    }

    public int getShippingFee(int orderItemsPrice) {
        return orderItemsPrice > 40000 ? 0 : 3000;
    }

    public void validateMemberOrder(PortOneOrder order, int clientTotal, Long memberId) {
        try {
            validateMemberId(memberId);
            validatePaymentId(order.getPaymentId());
            validateMemberOrderPrice(getOrderItemsPrice(order.getOrderItems()), order.getMemberCouponId(), order.getUsedPoint(), clientTotal, memberId);
        } catch (UsedPaymentIdException e) {
            throw new IllegalArgumentException(e.getMessage());
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

    //클라이언트 위변조 검증
    private void validateMemberOrderPrice(int orderItemsPrice, Long memberCouponId, Integer usedPoint, Integer clientTotal, Long memberId) {
        int SHIPPING_FEE = getShippingFee(orderItemsPrice);

        MemberCoupon memberCoupon = null;

        if (memberCouponId != null) {
            memberCoupon = memberCouponRepository.findWithById(memberCouponId)
                    .orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_COUPON));
            validateCoupon(memberCoupon, orderItemsPrice);
        }

        int orderPriceUsedCoupon = orderItemsPrice - getCouponPrice(memberCoupon, orderItemsPrice);
        validatePoint(usedPoint, memberId, orderPriceUsedCoupon, SHIPPING_FEE);

        int serverTotal = orderPriceUsedCoupon - usedPoint + SHIPPING_FEE;
        if (clientTotal != serverTotal)
            throw new OrderFailException("결제 금액이 위변조 됐습니다.");
    }

    //무료 주문은 PG사 결제를 안 거쳤으므로, 결제 취소 없음
    //1.쿠폰으로 무료 주문
    //2.포인트로 무료 주문
    //3.쿠폰+포인트로 무료 주문
    public void validateMemberFreeOrderPrice(int orderItemsPrice, Long memberCouponId, Integer usedPoint, Long memberId) {
        validateMemberId(memberId);
        int SHIPPING_FEE = getShippingFee(orderItemsPrice);

        MemberCoupon memberCoupon = null;

        if (memberCouponId != null) {
            memberCoupon = memberCouponRepository.findWithById(memberCouponId)
                    .orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_COUPON));
            validateCoupon(memberCoupon, orderItemsPrice);
        }

        int orderPriceUsedCoupon = orderItemsPrice - getCouponPrice(memberCoupon, orderItemsPrice);
        validatePoint(usedPoint, memberId, orderPriceUsedCoupon, SHIPPING_FEE);

        int serverTotal = SHIPPING_FEE + orderPriceUsedCoupon - usedPoint;

        if (serverTotal != 0)
            throw new OrderFailException("결제해야 할 금액이 0원이 아닙니다.");
    }

    //현재 서비스 정책상 비회원은 쿠폰,포인트를 못 씀
    public void validateGuestOrder(PortOneOrder order, int clientTotal) {
        try {
            validatePaymentId(order.getPaymentId());
            if (getOrderItemsPrice(order.getOrderItems()) != clientTotal)
                throw new OrderFailException("결제 금액이 위변조 됐습니다.");
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

    private void validatePoint(int usedPoint, Long memberId, int orderPriceUsedCoupon, int SHIPPING_FEE) {
        String cancelMsg = null;

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new OrderFailException(NOT_FOUND_MEMBER));

        int serverPoint = member.getPoint();

        if (usedPoint > serverPoint)
            cancelMsg = "보유한 포인트보다 넘게 사용할 수 없습니다.";
        else if (usedPoint > orderPriceUsedCoupon + SHIPPING_FEE)
            cancelMsg = "주문 가격보다 많은 포인트를 사용할 수 없습니다.";

        if (cancelMsg != null)
            throw new OrderFailException(cancelMsg);
    }

    private void validatePaymentId(String paymentId) {
        orderRepository.findByPaymentId(paymentId)
                .ifPresent(order -> {
                    throw new UsedPaymentIdException();
                });
    }

    private void validateMemberId(Long memberId) {
        if (memberId == null)
            throw new OrderFailException("memberId가 누락됐습니다");
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------

    public void updateOrderStatusToCompleted(List<Long> orderIds) {
        int count = em.createQuery("update Order o set o.status = :completed, o.updatedAt = now() where o.id in :orderIds")
                .setParameter("completed", OrderStatus.COMPLETED)
                .setParameter("orderIds", orderIds)
                .executeUpdate();
        log.info("{}건 자동 구매확정 처리 완료", count);
    }

}
