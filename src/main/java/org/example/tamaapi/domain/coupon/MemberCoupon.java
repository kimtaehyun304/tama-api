package org.example.tamaapi.domain.coupon;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tamaapi.domain.BaseEntity;
import org.example.tamaapi.domain.user.Member;

import java.time.LocalDate;

@Table(uniqueConstraints = {
        @UniqueConstraint(name = "coupon_member_unique", columnNames = {"coupon_id","member_id"})
})
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberCoupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_coupon_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private boolean isUsed;

    public MemberCoupon(Coupon coupon, Member member, boolean isUsed) {
        this.coupon = coupon;
        this.member = member;
        this.isUsed = isUsed;
    }

    public void changeIsUsed(boolean isUsed){
        this.isUsed = isUsed;
    }
}
