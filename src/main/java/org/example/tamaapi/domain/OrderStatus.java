package org.example.tamaapi.domain;

public enum OrderStatus {
    ORDER, READY, DELIVERY, COMPLETE, CANCEL, REFUND

    //ORDER 결제 완료
    //READY 택배사 전달
    //DELIVERY 배송 중
    //COMPLETE 배달 완료
    //주문 취소
}
