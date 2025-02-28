package org.example.tamaapi.dto.validator;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.tamaapi.dto.requestDto.MySort;
import org.example.tamaapi.exception.MyBadRequestException;
import org.example.tamaapi.jwt.TokenProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class TokenValidator {



}
