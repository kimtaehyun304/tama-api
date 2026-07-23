package org.example.tamaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.domain.order.*;
import org.example.tamaapi.domain.user.Member;
import org.example.tamaapi.domain.user.coupon.MemberCoupon;
import org.example.tamaapi.repository.MemberRepository;
import org.example.tamaapi.repository.order.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static org.example.tamaapi.util.ErrorMessageUtil.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderTxService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;

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

}