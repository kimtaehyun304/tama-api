package org.example.tamaapi.domain.order;

public enum OrderStatus {
    ORDER_RECEIVED,     // 주문 접수
    IN_DELIVERY,        // 배송 중 (출고 진행 과정 포함)
    DELIVERED,          // 배송 완료
    COMPLETED,          // 구매 확정
    CANCEL_RECEIVED,    // 취소 접수 (사용자가 취소를 접수하면 운영자가 상황에 따라 반품 or 환불로 결정)
    IN_RETURN,          // 반품 중
    RETURNED,           // 반품 완료
    IN_REFUND,          // 환불 중 (반품 없이 결제 환불. 예를 들어 상품 파손, 오염 등을 이유로)
    REFUNDED            // 환불 완료
}
