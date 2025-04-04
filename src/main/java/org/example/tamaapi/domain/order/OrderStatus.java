package org.example.tamaapi.domain.order;

public enum OrderStatus {
    PAYMENT, CHECK, DELIVERY, COMPLETE, CANCEL, REFUND

    //PAYMENT 결제 완료
    //CHECK 상품 준비중
    //DELIVERY 배송 중
    //COMPLETE 배송 완료
    //CANCEL 주문 취소
    //REFUND 환불
}
