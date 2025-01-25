package org.example.tamaapi.config;

import org.apache.coyote.BadRequestException;
import org.example.tamaapi.dto.responseDto.SimpleResponse;
import org.example.tamaapi.exception.MyBadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.sql.SQLIntegrityConstraintViolationException;

@RestControllerAdvice
public class CommonExceptionHandler {

    //예상 못한 에러 대비
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> exception(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new SimpleResponse(exception.getMessage()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Object> HttpRequestMethodNotSupportedException(Exception exception) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(new SimpleResponse(exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> IllegalArgumentException(Exception exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SimpleResponse(exception.getMessage()));
    }

    @ExceptionHandler(MyBadRequestException.class)
    public ResponseEntity<Object> MyBadRequestException(Exception exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SimpleResponse(exception.getMessage()));
    }

    //@RequestParam required 에러 (에러 영어로 나옴)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Object> MissingServletRequestParameterException(Exception exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SimpleResponse(exception.getMessage()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Object> NoResourceFoundException() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new SimpleResponse("존재하지 않는 API 입니다"));
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<Object> SQLIntegrityConstraintViolationException() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SimpleResponse("중복된 데이터입니다"));
    }





}
