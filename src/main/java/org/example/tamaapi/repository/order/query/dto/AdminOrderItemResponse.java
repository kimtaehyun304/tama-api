package org.example.tamaapi.repository.order.query.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.tamaapi.domain.order.OrderItem;
import org.example.tamaapi.dto.UploadFile;

import static org.example.tamaapi.util.ErrorMessageUtil.NOT_FOUND_IMAGE;

@Getter
@AllArgsConstructor
public class AdminOrderItemResponse {

    @JsonIgnore
    private Long orderId;

    private Long orderItemId;

    private String name;

    private String color;

    private String size;

    private int orderPrice;

    private int count;

    //대표 이미지
    private UploadFile uploadFile;

    private Boolean isReviewWritten;

    @QueryProjection
    public AdminOrderItemResponse(Boolean isReviewNotNull, Long orderId, Long orderItemId, Integer orderPrice, Integer count, String name, String color, String size) {
        this.orderId = orderId;
        this.orderItemId = orderItemId;
        this.name = name;
        this.color = color;
        this.size = size;
        this.orderPrice = orderPrice;
        this.count = count;

        /*
        //지연로딩
        uploadFile = orderItem.getColorItemSizeStock().getColorItem().getImages()
                .stream().filter(i -> i.getSequence() == 1).findFirst().orElseThrow(()-> new IllegalArgumentException(NOT_FOUND_IMAGE)).getUploadFile();

         */
        isReviewWritten = isReviewNotNull;

    }
}
