package org.example.tamaapi.dto.requestDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.example.tamaapi.domain.Gender;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
@ToString
public class CategoryItemFilterRequest {

    @PositiveOrZero
    Integer minPrice;

    @PositiveOrZero
    Integer maxPrice ;

    //아에 안오면 무관
    List<Long> colorIds;

    //하나만 올수도 있고, 두개 올수도 있음.
    //아에 안오면 무관, 혹은 두개와도 무관
    List<Gender> genders;

    //기본값 false -> 품절 포함x
    Boolean isContainSoldOut;

    List<MySort> sorts;

}
