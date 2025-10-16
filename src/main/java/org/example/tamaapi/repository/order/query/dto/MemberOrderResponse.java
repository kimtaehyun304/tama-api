package org.example.tamaapi.repository.order.query.dto;

import com.querydsl.core.annotations.QueryProjection;
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
public class MemberOrderResponse {

    //orderId
    private Long id;

    private LocalDateTime orderDate;

    private OrderStatus status;

    private DeliveryResponse delivery;

    private List<OrderItemResponse> orderItems = new ArrayList<>();

    private int usedCouponPrice;

    private int usedPoint;

    private int shippingFee;

    @QueryProjection
    public MemberOrderResponse(Order order) {
        id = order.getId();
        orderDate = order.getCreatedAt();
        status = order.getStatus();
        delivery = new DeliveryResponse(order.getDelivery());
        usedCouponPrice = order.getUsedCouponPrice();
        usedPoint = order.getUsedPoint();
        shippingFee = order.getShippingFee();
    }

}
