package org.example.tamaapi.dto.requestDto.order;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AdminOrderCond {

    private String memberName;

    private String itemName;

    private LocalDateTime orderDate;

    //MySort sort;

}
