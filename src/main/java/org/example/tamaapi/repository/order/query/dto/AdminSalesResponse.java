package org.example.tamaapi.repository.order.query.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class AdminSalesResponse {
    private List<AdminMonthSalesResponse> monthSales;
    private List<AdminCategorySalesResponse> categorySales;
}
