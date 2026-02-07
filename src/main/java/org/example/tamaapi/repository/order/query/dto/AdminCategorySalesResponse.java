package org.example.tamaapi.repository.order.query.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;
import org.example.tamaapi.domain.item.Category;

import java.time.LocalDateTime;

@Getter
@Setter
public class AdminCategorySalesResponse {


    private String categoryName;

    private Long orderCount;

    private Integer orderTotal;

    @QueryProjection
    public AdminCategorySalesResponse(String categoryName, Long orderCount, Integer orderTotal) {
        this.categoryName = categoryName;
        this.orderCount = orderCount;
        this.orderTotal = orderTotal;
    }
}
