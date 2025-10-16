package org.example.tamaapi.dto.responseDto;

import lombok.Getter;
import org.example.tamaapi.domain.user.coupon.CouponType;
import org.example.tamaapi.domain.user.coupon.MemberCoupon;

import java.time.LocalDate;

@Getter
public class MemberCouponResponse {

    private Long id;

    private CouponType type;

    // percent(1~100) or price
    private Integer discountValue;

    private LocalDate expiresAt;

    public MemberCouponResponse(MemberCoupon memberCoupon) {
        this.id = memberCoupon.getId();
        this.type = memberCoupon.getCoupon().getType();
        this.discountValue = memberCoupon.getCoupon().getDiscountValue();
        this.expiresAt = memberCoupon.getCoupon().getExpiresAt();
    }
}
