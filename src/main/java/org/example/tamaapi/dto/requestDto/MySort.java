package org.example.tamaapi.dto.requestDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.example.tamaapi.exception.MyBadRequestException;

@Getter
@AllArgsConstructor
@ToString
public class MySort {
    String property;
    String direction;
}
