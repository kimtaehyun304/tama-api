package org.example.tamaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.domain.item.ColorItemSizeStock;
import org.example.tamaapi.domain.order.*;
import org.example.tamaapi.domain.user.Member;
import org.example.tamaapi.domain.user.MemberAddress;
import org.example.tamaapi.domain.user.coupon.MemberCoupon;
import org.example.tamaapi.dto.requestDto.order.OrderItemRequest;
import org.example.tamaapi.dto.requestDto.order.OrderRequest;
import org.example.tamaapi.repository.JdbcTemplateRepository;
import org.example.tamaapi.repository.MemberAddressRepository;
import org.example.tamaapi.repository.MemberRepository;
import org.example.tamaapi.repository.item.ColorItemSizeStockRepository;
import org.example.tamaapi.repository.order.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.example.tamaapi.util.ErrorMessageUtil.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderTxService {

    private final OrderRepository orderRepository;
    private final ColorItemSizeStockRepository colorItemSizeStockRepository;
    private final MemberRepository memberRepository;
    private final MemberAddressRepository memberAddressRepository;
    private final JdbcTemplateRepository jdbcTemplateRepository;

    public void updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_ORDER));
        order.changeStatus(status);
    }

    public void refundAndRollbackCouponAndPoint(Long orderId) {
        Order order = orderRepository.findWithMemberCouponByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_ORDER));
        OrderStatus status = order.getStatus();

        //자동 환불 스케줄러 동작 중
        if(status == OrderStatus.PG_CANCEL_ERROR)
            throw new IllegalArgumentException("PG 서버 장애 복구되면 자동으로 환불됩니다");

        if (!(status == OrderStatus.ORDER_RECEIVED || status == OrderStatus.DELIVERED || status == OrderStatus.CANCEL_RECEIVED))
            throw new IllegalArgumentException("주문 취소 확정 가능 단계가 아닙니다");

        order.changeStatus(OrderStatus.REFUNDED);
        rollbackCouponAndPoint(order);
    }


    private void rollbackCouponAndPoint(Order order) {
        MemberCoupon memberCoupon = order.getMemberCoupon();
        int usedPoint = order.getUsedPoint();

        if(memberCoupon != null) memberCoupon.changeIsUsed(false);

        if(usedPoint != 0) {
            Member member = memberRepository.findById(order.getMember().getId())
                    .orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_MEMBER));
            member.plusPoint(usedPoint);
        }
    }

    @Transactional
    public void saveMockOrder() {
        SecureRandom secureRandom = new SecureRandom();

        //상품 pk 범위
        int minItemId = 400017;
        int maxItemId = 400076;
        Long randStockId = (long) (secureRandom.nextInt(maxItemId - minItemId + 1) + minItemId);

        //1은 운영자 pk
        int minMemberId = 2;
        int maxMemberId = 12;
        Long randMemberId = (long) (secureRandom.nextInt(maxMemberId - minMemberId + 1) + minMemberId);

        ColorItemSizeStock colorItemSizeStock = colorItemSizeStockRepository.findById(randStockId).get();
        Member member = memberRepository.findById(randMemberId).get();
        MemberAddress memberAddress = memberAddressRepository.findByMemberIdAndIsDefault(randMemberId, true).get();

        OrderRequest req = new OrderRequest("mock" + UUID.randomUUID().toString(), null, null,
                memberAddress.getReceiverNickName(), memberAddress.getReceiverPhone(), memberAddress.getZipCode()
                , memberAddress.getStreet(), memberAddress.getDetail(), "문 앞에 놔주세요", null, 0,
                List.of(
                        new OrderItemRequest(randStockId, 1)
                ));

        //배송 엔티티 생성
        Delivery delivery = new Delivery(req.getZipCode(), req.getStreetAddress(), req.getDetailAddress(), req.getDeliveryMessage()
                , req.getReceiverNickname(), req.getReceiverPhone());

        List<OrderItem> batchOrderItems = new ArrayList<>();
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequest orderItemRequest : req.getOrderItems()) {
            //가격 변동 or 할인 쿠폰 고려
            Integer nowPrice = colorItemSizeStock.getColorItem().getItem().getNowPrice();
            int orderPrice = nowPrice;

            OrderItem orderItem = OrderItem.builder().colorItemSizeStock(colorItemSizeStock).orderPrice(orderPrice)
                    .count(orderItemRequest.getOrderCount()).build();
            batchOrderItems.add(orderItem);
            orderItems.add(orderItem);
        }

        Order order = Order.createMemberOrder(req.getPaymentId(), member, delivery, null, 0, 0, 0, orderItems);
        orderRepository.save(order);
        jdbcTemplateRepository.saveOrderItems(batchOrderItems);
    }



}