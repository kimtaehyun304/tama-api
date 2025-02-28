package org.example.tamaapi.repository;

import org.example.tamaapi.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByPaymentId(String paymentId);

    @Query("select o from Order o join fetch o.member m join fetch o.delivery d where m.id = :memberId")
    List<Order> findAllWithMemberAndDeliveryByMemberId(Long memberId);
}
