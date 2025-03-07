package org.example.tamaapi.dto.responseDto.order;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.tamaapi.domain.Member;
import org.example.tamaapi.domain.Order;
import org.example.tamaapi.domain.OrderItem;
import org.example.tamaapi.domain.item.Color;
import org.example.tamaapi.domain.item.ColorItem;
import org.example.tamaapi.domain.item.ColorItemSizeStock;

@Getter
@AllArgsConstructor
public class OrderItemResponse {

    private String name;

    private String color;

    private String size;

    private int orderPrice;

    private int count;

    //대표 이미지
    private String imageSrc;

    public OrderItemResponse(OrderItem orderItem) {
        name = orderItem.getColorItemSizeStock().getColorItem().getItem().getName();
        color = orderItem.getColorItemSizeStock().getColorItem().getColor().getName();
        size = orderItem.getColorItemSizeStock().getSize();
        orderPrice = orderItem.getOrderPrice();
        count = orderItem.getCount();
        imageSrc = orderItem.getColorItemSizeStock().getColorItem().getImageSrc();
    }
}
