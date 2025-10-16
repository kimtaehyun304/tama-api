package org.example.tamaapi.repository;

import org.example.tamaapi.domain.user.coupon.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {


}
