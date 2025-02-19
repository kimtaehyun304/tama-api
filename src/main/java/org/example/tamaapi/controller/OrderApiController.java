package org.example.tamaapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tamaapi.domain.item.Color;
import org.example.tamaapi.domain.item.ColorItemSizeStock;
import org.example.tamaapi.dto.requestDto.MemberOrderRequest;
import org.example.tamaapi.dto.requestDto.OrderItemRequest;
import org.example.tamaapi.dto.requestDto.OrderRequest;
import org.example.tamaapi.dto.responseDto.SimpleResponse;
import org.example.tamaapi.dto.responseDto.item.ColorResponse;
import org.example.tamaapi.dto.validator.PaymentValidator;
import org.example.tamaapi.exception.MyBadRequestException;
import org.example.tamaapi.jwt.TokenProvider;
import org.example.tamaapi.repository.*;
import org.example.tamaapi.service.CacheService;
import org.example.tamaapi.service.OrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final ColorItemSizeStockRepository colorItemSizeStockRepository;
    private final PaymentValidator paymentValidator;
    private final OrderService orderService;

    @PostMapping("/api/orders/member")
    public ResponseEntity<Object> saveOrder(@Valid @RequestBody MemberOrderRequest memberOrderRequest, Principal principal) {

        if (principal == null || !StringUtils.hasText(principal.getName()))
            throw new MyBadRequestException("액세스 토큰이 비었습니다.");

        paymentValidator.validate(memberOrderRequest.getPaymentId(), memberOrderRequest.getOrderItems());

        Long memberId = Long.parseLong(principal.getName());

        orderService.saveMemberOrder(
                memberId,
                memberOrderRequest.getReceiverNickname(),
                memberOrderRequest.getReceiverPhone(),
                memberOrderRequest.getZipCode(),
                memberOrderRequest.getStreetAddress(),
                memberOrderRequest.getDetailAddress(),
                memberOrderRequest.getDeliveryMessage(),
                memberOrderRequest.getOrderItems()
        );

        return ResponseEntity.status(HttpStatus.OK).body(new SimpleResponse("결제 완료"));
    }

}
