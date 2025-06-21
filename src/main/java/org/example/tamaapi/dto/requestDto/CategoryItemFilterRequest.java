package org.example.tamaapi.dto.requestDto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.example.tamaapi.domain.Gender;
import org.example.tamaapi.exception.MyBadRequestException;
import org.springframework.util.StringUtils;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class CategoryItemFilterRequest {

    @PositiveOrZero
    private Integer minPrice;

    @PositiveOrZero
    private Integer maxPrice;

    //아에 안오면 무관
    private List<Long> colorIds;

    //하나만 올수도 있고, 두개 올수도 있음.
    //아에 안오면 무관, 혹은 두개와도 무관
    private List<Gender> genders;

    //기본값 품절 포함x
    private Boolean isContainSoldOut;

    private String itemName;

    //MySort sort;

}
