package org.example.tamaapi.repository.order.query.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
public class AdminMonthSalesResponse {


    private Date orderDate;

    private Long orderCount;

    private Integer orderTotal;

    @QueryProjection
    public AdminMonthSalesResponse(Date orderDate, Long orderCount, Integer orderTotal) {
        this.orderDate = orderDate;
        this.orderCount = orderCount;
        this.orderTotal = orderTotal;
    }
}
