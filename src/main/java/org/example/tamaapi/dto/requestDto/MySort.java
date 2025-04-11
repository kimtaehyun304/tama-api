package org.example.tamaapi.dto.requestDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.example.tamaapi.exception.MyBadRequestException;
import org.springframework.data.domain.Sort;

@Getter
@AllArgsConstructor
@ToString
public class MySort {
    private String property;
    private Sort.Direction direction;
}
