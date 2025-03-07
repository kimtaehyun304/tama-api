package org.example.tamaapi.dto.responseDto.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.tamaapi.domain.Order;
import org.example.tamaapi.domain.OrderStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public class OrderResponse {

    //orderId
    private Long id;

    private LocalDateTime orderDate;

    private OrderStatus status;

    private DeliveryResponse delivery;

    private List<OrderItemResponse> orderItems = new ArrayList<>();

    public OrderResponse(Order order) {
        id = order.getId();
        orderDate = order.getCreatedAt();
        status = order.getStatus();
        delivery = new DeliveryResponse(order.getDelivery());
        orderItems.addAll(order.getOrderItems().stream().map(OrderItemResponse::new).toList());
    }
}
