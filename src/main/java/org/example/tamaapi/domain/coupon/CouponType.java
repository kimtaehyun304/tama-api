package org.example.tamaapi.domain.coupon;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tamaapi.domain.BaseEntity;

//그룹바이에 복합 인덱스 적용하려면 PK도 같이 있어야함



public enum CouponType {
    PERCENT_DISCOUNT, FIXED_DISCOUNT
}
