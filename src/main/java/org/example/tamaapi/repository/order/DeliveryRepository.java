package org.example.tamaapi.repository.order;

import org.example.tamaapi.domain.Delivery;
import org.example.tamaapi.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {


}
