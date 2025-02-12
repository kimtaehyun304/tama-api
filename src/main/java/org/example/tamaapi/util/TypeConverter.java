package org.example.tamaapi.util;

import org.example.tamaapi.exception.MyBadRequestException;
import org.springframework.data.domain.Sort;

public class TypeConverter {

    public static Sort.Direction convertStringToDirection(String direction) {

        switch (direction.toUpperCase()) {
            case "ASC":
                return Sort.Direction.ASC;
            case "DESC":
                return Sort.Direction.DESC;
            default:
                throw new MyBadRequestException("convertStringToDirection error");
        }
    }

}
