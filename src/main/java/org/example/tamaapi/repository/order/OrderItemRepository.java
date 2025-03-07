package org.example.tamaapi.repository.order;

import org.example.tamaapi.domain.Order;
import org.example.tamaapi.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {


}
