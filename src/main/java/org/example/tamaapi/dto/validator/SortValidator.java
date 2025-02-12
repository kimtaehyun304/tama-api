package org.example.tamaapi.dto.validator;

import org.example.tamaapi.dto.requestDto.MySort;
import org.example.tamaapi.exception.MyBadRequestException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class SortValidator {

    //MySort 컨버터 거쳐서 옴
    //컨버터에서 예외 던지니까 에러나서 SortValidator 만듬 -> BindingResult 필요없어서 커스텀 validator 만듬
    public void validate(Object target) {
        MySort sort = (MySort) target;
        String direction = sort.getDirection();;

        if (StringUtils.hasText(direction)){
            if(!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc"))
                throw new MyBadRequestException("sort direction은 'asc' 또는 'desc' 이어야 합니다.");
        }

    }
}
