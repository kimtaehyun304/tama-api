package org.example.tamaapi.dto.validator;

import org.example.tamaapi.dto.requestDto.MySort;
import org.example.tamaapi.exception.MyBadRequestException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class SortValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return MySort.class.isAssignableFrom(clazz);
    }

    //MySort 컨버터 거쳐서 옴
    //컨버터에서 예외 던지니까 에러나서 SortValidator 만듬
    @Override
    public void validate(Object target, Errors errors) {
        MySort sort = (MySort) target;
        //정렬이 필수는 아님

        if(sort != null) {
            if (!StringUtils.hasText(sort.getProperty()) && !StringUtils.hasText(sort.getDirection()))
                return;

            if (!StringUtils.hasText(sort.getProperty()))
                throw new MyBadRequestException("sort property가 누락됐습니다.");
            //errors.rejectValue("sort.property", "required");

            if (!StringUtils.hasText(sort.getDirection())) {
                throw new MyBadRequestException("sort direction이 누락됐습니다.");
                //errors.rejectValue("sort.direction", "required");
            } else if (!sort.getDirection().equalsIgnoreCase("asc") && !sort.getDirection().equalsIgnoreCase("desc")) {
                throw new MyBadRequestException("sort direction은 'asc' 또는 'desc' 이어야 합니다.");
                //errors.rejectValue("sort.direction","pattern");
            }
        }
    }
}
