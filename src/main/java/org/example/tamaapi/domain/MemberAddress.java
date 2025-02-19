package org.example.tamaapi.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//내 주소 설정
@Entity
@Getter @Setter
@Table(name = "delvery_address", uniqueConstraints = {
        @UniqueConstraint(name = "zipcode_street_detail_unique",columnNames = {"zipCode", "street", "detail"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberAddress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_address_id")
    private Long id;

    // 우편번호
    private String zipCode;

    // 도로명 주소
    private String street;

    // 상세 주소
    private String detail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

}


