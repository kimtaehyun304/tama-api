package org.example.tamaapi.repository.order.query.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChildCategorySalesResponse {

    @JsonIgnore
    private String parentName;

    private String categoryName;

    private Long orderCount;

    private Integer orderTotal;

    @QueryProjection
    public ChildCategorySalesResponse(String parentName, String categoryName, Long orderCount, Integer orderTotal) {
        this.parentName = parentName;
        this.categoryName = categoryName;
        this.orderCount = orderCount;
        this.orderTotal = orderTotal;
    }

}
