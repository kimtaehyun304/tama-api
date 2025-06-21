package org.example.tamaapi.dto.responseDto.order;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.tamaapi.domain.order.Delivery;
import org.example.tamaapi.domain.order.Order;
import org.example.tamaapi.domain.order.OrderStatus;
import org.example.tamaapi.dto.responseDto.order.DeliveryResponse;
import org.example.tamaapi.dto.responseDto.order.OrderItemResponse;
import org.example.tamaapi.repository.order.query.dto.AdminOrderItemResponse;
import org.springframework.util.StringUtils;

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

    private List<AdminOrderItemResponse> orderItems = new ArrayList<>();

    @QueryProjection
    public AdminOrderResponse(Order order, String nickname) {
        id = order.getId();
        orderDate = order.getCreatedAt();
        status = order.getStatus();
        buyerName = StringUtils.hasText(nickname) ? nickname : order.getGuest().getNickname();
        delivery = new DeliveryResponse(order.getDelivery());
    }

    public AdminOrderResponse(Order order) {
        id = order.getId();
        orderDate = order.getCreatedAt();
        status = order.getStatus();
        buyerName = order.getMember() != null ? order.getMember().getNickname() : order.getGuest().getNickname();
        delivery = new DeliveryResponse(order.getDelivery());
    }
}
