package org.example.tamaapi.dto.responseDto;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tamaapi.domain.coupon.Coupon;
import org.example.tamaapi.domain.coupon.CouponType;
import org.example.tamaapi.domain.coupon.MemberCoupon;
import org.example.tamaapi.domain.item.ColorItem;
import org.example.tamaapi.domain.item.ColorItemImage;
import org.example.tamaapi.domain.user.Member;
import org.example.tamaapi.dto.UploadFile;
import org.example.tamaapi.dto.responseDto.item.ColorItemSizeStockDto;
import org.example.tamaapi.dto.responseDto.item.ItemDto;
import org.example.tamaapi.dto.responseDto.item.RelatedColorItemDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
