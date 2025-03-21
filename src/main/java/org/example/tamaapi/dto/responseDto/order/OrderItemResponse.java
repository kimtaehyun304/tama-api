package org.example.tamaapi.dto.responseDto.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import org.example.tamaapi.dto.UploadFile;

import static org.example.tamaapi.util.ErrorMessageUtil.NOT_FOUND_IMAGE;

@Getter
@AllArgsConstructor
public class OrderItemResponse {

    @JsonIgnore
    private Long orderId;

    private String name;

    private String color;

    private String size;

    private int orderPrice;

    private int count;

    //대표 이미지
    private UploadFile uploadFile;

    public OrderItemResponse(OrderItem orderItem) {
        orderId = orderItem.getOrder().getId();
        name = orderItem.getColorItemSizeStock().getColorItem().getItem().getName();
        color = orderItem.getColorItemSizeStock().getColorItem().getColor().getName();
        size = orderItem.getColorItemSizeStock().getSize();
        orderPrice = orderItem.getOrderPrice();
        count = orderItem.getCount();
        //지연로딩
        uploadFile = orderItem.getColorItemSizeStock().getColorItem().getImages()
                .stream().filter(i -> i.getSequence() == 1).findFirst().orElseThrow(()-> new IllegalArgumentException(NOT_FOUND_IMAGE)).getUploadFile();
    }
}
