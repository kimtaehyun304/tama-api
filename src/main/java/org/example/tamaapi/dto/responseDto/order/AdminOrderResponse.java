package org.example.tamaapi.dto.responseDto.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.tamaapi.domain.order.Order;
import org.example.tamaapi.domain.order.OrderStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class AdminOrderResponse {

    //orderId
    private Long id;

    private LocalDateTime orderDate;

    private OrderStatus status;

    private String buyerName;

    private DeliveryResponse delivery;

    private List<OrderItemResponse> orderItems = new ArrayList<>();

    public AdminOrderResponse(Order order) {
        id = order.getId();
        orderDate = order.getCreatedAt();
        status = order.getStatus();
        buyerName = order.getMember() != null ? order.getMember().getNickname() : order.getGuest().getNickname();
        delivery = new DeliveryResponse(order.getDelivery());
    }
}
