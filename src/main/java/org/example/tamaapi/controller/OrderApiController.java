package org.example.tamaapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.dto.requestDto.order.GuestOrderRequest;
import org.example.tamaapi.dto.requestDto.order.MemberOrderRequest;

import org.example.tamaapi.dto.responseDto.SimpleResponse;
import org.example.tamaapi.dto.validator.PaymentValidator;
import org.example.tamaapi.exception.MyBadRequestException;
import org.example.tamaapi.repository.*;
import org.example.tamaapi.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OrderApiController {

    private final ColorItemSizeStockRepository colorItemSizeStockRepository;
    private final PaymentValidator paymentValidator;
    private final OrderService orderService;

    @PostMapping("/api/orders/member")
    public ResponseEntity<Object> saveMemberOrder(@Valid @RequestBody MemberOrderRequest memberOrderRequest, BindingResult bindingResult, Principal principal) {

        StringBuilder message = new StringBuilder();

        //결제는 memberOrderRequest 없어도 가능함. 근데 주문은 memberOrderRequest 없으면 결제 취소해야함
        if (bindingResult.hasFieldErrors()) {
            for (FieldError fieldError : bindingResult.getFieldErrors())
                message.append(fieldError.getField()).append("는(은) ").append(fieldError.getDefaultMessage()).append(". ");
        }

        if (principal == null || !StringUtils.hasText(principal.getName()))
            message.append("액세스 토큰이 비었습니다. ");

        if(bindingResult.hasFieldErrors() || principal == null || !StringUtils.hasText(principal.getName())){
            if (StringUtils.hasText(memberOrderRequest.getPaymentId())) {
                orderService.cancelPortOnePayment(memberOrderRequest.getPaymentId());
                message.append("결제 자동 취소! ");
            } else {
                String msg = "paymentId 누락으로 결제 취소 불가! ";
                message.append(msg);
                log.info(msg+memberOrderRequest);
            }
            throw new MyBadRequestException(message.toString());
        }

        Long memberId = Long.parseLong(principal.getName());
        orderService.saveMemberOrder(
                memberOrderRequest.getPaymentId(),
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

    @PostMapping("/api/orders/guest")
    public ResponseEntity<Object> saveGuestOrder(@Valid @RequestBody GuestOrderRequest guestOrderRequest, BindingResult bindingResult) {

        StringBuilder message = new StringBuilder();

        //결제는 memberOrderRequest 없어도 가능함. 근데 주문은 memberOrderRequest 없으면 결제 취소해야함
        if (bindingResult.hasFieldErrors()) {
            if (StringUtils.hasText(guestOrderRequest.getPaymentId())) {
                orderService.cancelPortOnePayment(guestOrderRequest.getPaymentId());
                message.append("결제 자동 취소! ");
            } else {
                String msg = "paymentId 누락으로 결제 취소 불가! ";
                message.append(msg);
                log.info(msg+guestOrderRequest);
            }
            throw new MyBadRequestException(message.toString());
        }

        orderService.saveGuestOrder(
                guestOrderRequest.getPaymentId(),
                guestOrderRequest.getSenderNickname(),
                guestOrderRequest.getSenderEmail(),
                guestOrderRequest.getSenderPhone(),
                guestOrderRequest.getReceiverNickname(),
                guestOrderRequest.getReceiverPhone(),
                guestOrderRequest.getZipCode(),
                guestOrderRequest.getStreetAddress(),
                guestOrderRequest.getDetailAddress(),
                guestOrderRequest.getDeliveryMessage(),
                guestOrderRequest.getOrderItems()
        );

        return ResponseEntity.status(HttpStatus.OK).body(new SimpleResponse("결제 완료"));
    }

    //localhost는 webhook 못씀
    //결제가 되면 포트원이 tama 엔드포인트 호출. 즉 리엑트에서 호출하는게 아니므로 포트원 결제 내역에 필요한 주문 정보를 다 저장해야함
    //webhook은 안전하지만 포트원에게 너무 많은 정보를 제공하는 것 같음. 팀원이랑 상의 필요
    @PostMapping("/api/orders/member/webhook")
    public void webhook() {

    }

}
