package org.example.tamaapi.repository.order;

import org.example.tamaapi.domain.order.Order;
import org.example.tamaapi.domain.order.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByPaymentId(String paymentId);

    @Query("select o from Order o join fetch o.member m join fetch o.delivery d where m.id = :memberId")
    Page<Order> findAllWithMemberAndDeliveryByMemberId(Long memberId, Pageable pageable);

    @Query("select o from Order o join fetch o.orderItems oi join fetch o.delivery d where o.id = :orderId")
    Optional<Order> findAllWithOrderItemAndDeliveryByOrderId(Long orderId);

    //배송완료일로부터 8일이 넘은 주문 조회
    //-0한건 숫자형으로 바꾸기 위함. (안해도 비교 가능하지만, 다른 버전에서 안될까봐)
    @Query("select o from Order o where o.status = 'IN_DELIVERY' and date(o.updatedAt)-0 >= date(now())-8")
    List<Order> findInDeliveryOrdersAfter8Days();
}
