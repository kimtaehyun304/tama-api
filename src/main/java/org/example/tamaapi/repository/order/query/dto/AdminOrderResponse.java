package org.example.tamaapi.repository.order.query.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.tamaapi.domain.order.Order;
import org.example.tamaapi.domain.order.OrderStatus;
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

    private List<OrderItemResponse> orderItems = new ArrayList<>();

    @QueryProjection
    public AdminOrderResponse(Order order, String nickname) {
        id = order.getId();
        orderDate = order.getCreatedAt();
        status = order.getStatus();
        buyerName = StringUtils.hasText(nickname) ? nickname : order.getGuest().getNickname();
        delivery = new DeliveryResponse(order.getDelivery());
    }

}
