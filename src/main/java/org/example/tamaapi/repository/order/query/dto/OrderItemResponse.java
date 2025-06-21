package org.example.tamaapi.repository.order.query.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.tamaapi.dto.UploadFile;

@Getter
@Setter
@AllArgsConstructor
public class OrderItemResponse {

    @JsonIgnore
    private Long orderId;
    @JsonIgnore
    private Long colorItemId;

    private Long orderItemId;

    private String name;

    private String color;

    private String size;

    private int orderPrice;

    private int count;

    private Boolean isReviewWritten;

    //대표 이미지
    private UploadFile uploadFile;


    @QueryProjection
    public OrderItemResponse(Long orderId, Long colorItemId, Long orderItemId, Integer orderPrice, Integer count, String name, String color, String size, Boolean isReviewNotNull) {
        this.orderId = orderId;
        this.colorItemId = colorItemId;
        this.orderItemId = orderItemId;
        this.name = name;
        this.color = color;
        this.size = size;
        this.orderPrice = orderPrice;
        this.count = count;
        isReviewWritten = isReviewNotNull;
    }
}
