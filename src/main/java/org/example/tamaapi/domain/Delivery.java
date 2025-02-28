package org.example.tamaapi.domain;

import jakarta.persistence.*;
import lombok.*;

//상세 주소를 그때마다 다르게 적을수도 있어서 엔티티로 안했음
@Entity
@Getter @Setter
/*
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "zipcode_street_detail_unique",columnNames = {"zipCode", "street", "detail"})
})
 */
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

    // 상세 주소
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


