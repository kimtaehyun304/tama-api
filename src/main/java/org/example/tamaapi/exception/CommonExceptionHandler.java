package org.example.tamaapi.exception;

import org.apache.coyote.BadRequestException;
import org.example.tamaapi.dto.responseDto.SimpleResponse;
import org.example.tamaapi.exception.MyBadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.util.StringUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class CommonExceptionHandler {

    @ExceptionHandler(IOException.class)
    public ResponseEntity<SimpleResponse> IOException(IOException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new SimpleResponse(exception.getMessage()));
    }

    //UnException error
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<SimpleResponse> handleValidationExceptions(MethodArgumentNotValidException exception) {
        StringBuilder message = new StringBuilder();

        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()){
                message.append(fieldError.getField()).append("는(은) ").append(fieldError.getDefaultMessage()).append(". ");
        }

        return ResponseEntity.badRequest().body(new SimpleResponse(message.toString()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<SimpleResponse> HttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(new SimpleResponse(exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<SimpleResponse> IllegalArgumentException(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SimpleResponse(exception.getMessage()));
    }

    @ExceptionHandler(MyBadRequestException.class)
    public ResponseEntity<SimpleResponse> MyBadRequestException(MyBadRequestException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SimpleResponse(exception.getMessage()));
    }

    //@RequestParam required 에러 (에러 영어로 나오나 클래스 노출 없음)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<SimpleResponse> MissingServletRequestParameterException(MissingServletRequestParameterException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SimpleResponse(exception.getMessage()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<SimpleResponse> NoResourceFoundException() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new SimpleResponse("존재하지 않는 API 입니다"));
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<SimpleResponse> SQLIntegrityConstraintViolationException() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SimpleResponse("저장 실패"));
    }


    @ExceptionHandler(MyExpiredJwtException.class)
    public ResponseEntity<SimpleResponse> MyExpiredJwtException(MyExpiredJwtException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SimpleResponse(exception.getMessage()));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<SimpleResponse> AuthorizationDeniedException(AuthorizationDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new SimpleResponse(exception.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<SimpleResponse> UnauthorizedException(UnauthorizedException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new SimpleResponse(exception.getMessage()));
    }

}
