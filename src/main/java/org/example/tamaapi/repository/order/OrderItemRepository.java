package org.example.tamaapi.repository.order;

import org.example.tamaapi.domain.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("select oi from OrderItem oi" +
            " join fetch oi.colorItemSizeStock ciss join fetch ciss.colorItem ci join fetch ci.color cl join fetch ci.item" +
            " where oi.order.id in :orderIds")
    List<OrderItem> findAllWithByOrderIdIn(List<Long> orderIds);


    @Query("select oi from OrderItem oi" +
            " join fetch oi.colorItemSizeStock ciss join fetch ciss.colorItem ci join fetch ci.color cl join fetch ci.item" +
            " where oi.order.id = :orderId")
    List<OrderItem> findAllWithByOrderId(Long orderId);

    @Query("select oi from OrderItem oi join fetch oi.order o where oi.id = :orderItemId")
    Optional<OrderItem> findWithOrderById(Long orderItemId);
}
