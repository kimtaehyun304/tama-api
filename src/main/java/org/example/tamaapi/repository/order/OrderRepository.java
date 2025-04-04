package org.example.tamaapi.repository.order;

import org.example.tamaapi.domain.order.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByPaymentId(String paymentId);

    @Query("select o from Order o join fetch o.member m join fetch o.delivery d where m.id = :memberId")
    Page<Order> findAllWithMemberAndDeliveryByMemberId(Long memberId, Pageable pageable);

    @Query("select o from Order o join fetch o.orderItems oi join fetch o.delivery d where o.id = :orderId")
    Optional<Order> findAllWithOrderItemAndDeliveryByOrderId(Long orderId);

    @Query("select o from Order o left join fetch o.member m join fetch o.delivery d")
    Page<Order> findAllWithMemberAndDelivery(Pageable pageable);
}
