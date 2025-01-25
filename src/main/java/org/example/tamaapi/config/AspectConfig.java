package org.example.tamaapi.config;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.example.tamaapi.dto.responseDto.SimpleResponse;
import org.example.tamaapi.exception.MyBadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

@Component
@Aspect
public class AspectConfig {

    //MethodArgumentNotValidException 처리
    //AOP 없이 MethodArgumentNotValidException 공통예외 처리가능하나 에러메시지 가독성 별로 -> bindingResult로 에러메시지 가공
    //* org.example.tamaapi.controller.* 안됨
    //throw new MethodArgumentNotValidException() 안됨. return ResponseEntity 안됨
    @Before("execution(* org.example.tamaapi.controller..*(.., @jakarta.validation.Valid (*), ..)))")
    public void validationAspect(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        StringBuilder message = new StringBuilder();
        for (Object arg : args) {
            if (arg instanceof BindingResult) {
                BindingResult bindingResult = (BindingResult) arg;
                if(bindingResult.hasErrors()) {
                    for (FieldError fieldError : bindingResult.getFieldErrors())
                        message.append(fieldError.getField()).append("는(은) ").append(fieldError.getDefaultMessage()).append(". ");
                    // throw BadRequestException 안돼서 예외 만듬
                    throw new MyBadRequestException(message.toString());
                }
            }
        }
    }
}
