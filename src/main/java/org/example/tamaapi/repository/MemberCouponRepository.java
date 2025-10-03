package org.example.tamaapi.repository;

import org.example.tamaapi.domain.coupon.Coupon;
import org.example.tamaapi.domain.coupon.MemberCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MemberCouponRepository extends JpaRepository<MemberCoupon, Long> {
    
    @Query("""
            select m from MemberCoupon m join fetch m.coupon c
            where m.member.id = :memberId and m.isUsed = false and c.expiresAt >= now() 
            """)
    List<MemberCoupon> findNotExpiredAndUnusedCouponsByMemberId(Long memberId);

}
