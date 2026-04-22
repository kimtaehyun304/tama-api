package org.example.tamaapi.domain.order;

import jakarta.persistence.*;
import lombok.*;
import org.example.tamaapi.domain.BaseEntity;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
public class Delivery extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    private Long id;

    // 우편번호
    @Column(nullable = false)
    private String zipCode;

    // 도로명 주소
    @Column(nullable = false)
    private String street;

    @Column(nullable = false)
    private String detail;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private String receiverNickname;

    @Column(nullable = false)
    private String receiverPhone;

    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "enum()")
    private Courier courier;

    //배송 상태는 주문 상태에서 관리

    public Delivery(String zipCode, String street, String detail, String message, String receiverNickname, String receiverPhone) {
        this.zipCode = zipCode;
        this.street = street;
        this.detail = detail;
        this.message = message;
        this.receiverNickname = receiverNickname;
        this.receiverPhone = receiverPhone;
    }

    public void changeTracking(Courier courier, String trackingNumber){
        this.courier = courier;
        this.trackingNumber = trackingNumber;
    }

    public void setIdByBatchId(Long id) {
        this.id = id;
    }
}


