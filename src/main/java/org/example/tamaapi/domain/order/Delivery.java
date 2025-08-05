package org.example.tamaapi.domain.order;

import jakarta.persistence.*;
import lombok.*;
import org.example.tamaapi.domain.BaseEntity;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Delivery extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    private Long id;

    // 우편번호
    private String zipCode;

    // 도로명 주소
    private String street;

    private String detail;

    private String message;

    private String receiverNickname;

    private String receiverPhone;

    public Delivery(String zipCode, String street, String detail, String message, String receiverNickname, String receiverPhone) {
        this.zipCode = zipCode;
        this.street = street;
        this.detail = detail;
        this.message = message;
        this.receiverNickname = receiverNickname;
        this.receiverPhone = receiverPhone;
    }
}


