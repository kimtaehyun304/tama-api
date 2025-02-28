package org.example.tamaapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.tamaapi.dto.requestDto.MySort;
import org.example.tamaapi.exception.MyBadRequestException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Sort;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToMySortConverter());
    }

    private class StringToMySortConverter implements Converter<String, MySort> {

        //MyBadRequestException 발생하면 MethodArgumentNotValidException가 먼저 실행됨. 에러 메시지 전달 불가
        //검증은 SortValidator에서 함
        @Override
        public MySort convert(String source) {
            String[] parts = source.split(",");
            MySort mySort = new MySort(null, null);
            switch (parts.length){
                case 1 -> mySort = new MySort(parts[0], null);
                case 2 -> mySort = new MySort(parts[0], Sort.Direction.fromString(parts[1]));
            }
            return mySort;
        }
    }


}
