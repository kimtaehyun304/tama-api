package org.example.tamaapi.domain.user.coupon;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tamaapi.domain.BaseEntity;

import java.time.LocalDate;

//그룹바이에 복합 인덱스 적용하려면 PK도 같이 있어야함

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponType type;

    @Column(nullable = false)
    // percent(1~100) or price
    private Integer discountValue;

    @Column(nullable = false)
    private LocalDate expiresAt;

    public Coupon(CouponType type, Integer discountValue, LocalDate expiresAt) {
        this.type = type;
        this.discountValue = discountValue;
        this.expiresAt = expiresAt;
    }
}
