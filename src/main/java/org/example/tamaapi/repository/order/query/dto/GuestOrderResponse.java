package org.example.tamaapi.repository.order.query.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minidev.json.annotate.JsonIgnore;
import org.example.tamaapi.domain.order.Order;
import org.example.tamaapi.domain.order.OrderStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class GuestOrderResponse {

    //orderId
    private Long id;

    @JsonIgnore
    private String guestName;

    private LocalDateTime orderDate;

    private OrderStatus status;

    private DeliveryResponse delivery;

    private List<OrderItemResponse> orderItems = new ArrayList<>();

    @QueryProjection
    public GuestOrderResponse(Order order) {
        id = order.getId();
        guestName = order.getGuest().getNickname();
        orderDate = order.getCreatedAt();
        status = order.getStatus();
        delivery = new DeliveryResponse(order.getDelivery());
    }

}
