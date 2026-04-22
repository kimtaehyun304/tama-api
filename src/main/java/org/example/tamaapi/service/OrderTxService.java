package org.example.tamaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.domain.order.*;
import org.example.tamaapi.repository.order.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static org.example.tamaapi.util.ErrorMessageUtil.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderTxService {

    private final OrderRepository orderRepository;


    public void updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_ORDER));
        order.changeStatus(status);
    }

}