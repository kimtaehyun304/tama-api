package org.example.tamaapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.domain.Member;
import org.example.tamaapi.domain.Order;
import org.example.tamaapi.dto.requestDto.CustomPageRequest;
import org.example.tamaapi.dto.requestDto.order.CancelMemberOrderRequest;
import org.example.tamaapi.dto.requestDto.order.SaveGuestOrderRequest;
import org.example.tamaapi.dto.requestDto.order.SaveMemberOrderRequest;

import org.example.tamaapi.dto.responseDto.CustomPage;
import org.example.tamaapi.dto.responseDto.SimpleResponse;
import org.example.tamaapi.dto.responseDto.order.OrderResponse;
import org.example.tamaapi.dto.validator.PaymentValidator;
import org.example.tamaapi.exception.MyBadRequestException;
import org.example.tamaapi.repository.*;
import org.example.tamaapi.repository.item.ColorItemSizeStockRepository;
import org.example.tamaapi.repository.order.OrderRepository;
import org.example.tamaapi.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

import static org.example.tamaapi.util.ErrorMessageUtil.NOT_FOUND_MEMBER;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OrderApiController {

    private final ColorItemSizeStockRepository colorItemSizeStockRepository;
    private final PaymentValidator paymentValidator;
    private final OrderService orderService;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;


    //멤버 주문 조회
    @GetMapping("/api/orders/member")
    public CustomPage<OrderResponse> orders(Principal principal, @Valid @ModelAttribute CustomPageRequest customPageRequest) {
        if (principal == null || !StringUtils.hasText(principal.getName()))
            throw new IllegalArgumentException("액세스 토큰이 비었습니다.");

        Long memberId = Long.parseLong(principal.getName());
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_MEMBER));

        PageRequest pageRequest = PageRequest.of(customPageRequest.getPage() - 1, customPageRequest.getSize());
        Page<Order> orders = orderRepository.findAllWithMemberAndDeliveryByMemberId(memberId, pageRequest);
        List<OrderResponse> orderResponses = orders.stream().map(OrderResponse::new).toList();
        return new CustomPage<>(orderResponses, orders.getPageable(), orders.getTotalPages(), orders.getTotalElements());
    }

    //멤버 주문 저장
    @PostMapping("/api/orders/member")
    public ResponseEntity<Object> saveMemberOrder(@Valid @RequestBody SaveMemberOrderRequest saveMemberOrderRequest, BindingResult bindingResult, Principal principal) {

        StringBuilder message = new StringBuilder();

        //밑에서 주문 취소
        if (principal == null || !StringUtils.hasText(principal.getName()))
            message.append("액세스 토큰이 비었습니다. ");

        //결제는 memberOrderRequest 없어도 가능함. 근데 주문은 memberOrderRequest 없으면 결제 취소해야함
        if (bindingResult.hasFieldErrors()) {
            for (FieldError fieldError : bindingResult.getFieldErrors())
                message.append(fieldError.getField()).append("는(은) ").append(fieldError.getDefaultMessage()).append(". ");
        }

        if (bindingResult.hasFieldErrors() || principal == null || !StringUtils.hasText(principal.getName())) {
            if (StringUtils.hasText(saveMemberOrderRequest.getPaymentId())) {
                orderService.cancelPortOnePayment(saveMemberOrderRequest.getPaymentId());
                message.append("결제 자동 취소! ");
            } else {
                String msg = "paymentId 누락으로 결제 취소 불가! ";
                message.append(msg);
                log.info(msg + saveMemberOrderRequest);
            }
            throw new MyBadRequestException(message.toString());
        }

        Long memberId = Long.parseLong(principal.getName());
        orderService.saveMemberOrder(
                saveMemberOrderRequest.getPaymentId(),
                memberId,
                saveMemberOrderRequest.getReceiverNickname(),
                saveMemberOrderRequest.getReceiverPhone(),
                saveMemberOrderRequest.getZipCode(),
                saveMemberOrderRequest.getStreetAddress(),
                saveMemberOrderRequest.getDetailAddress(),
                saveMemberOrderRequest.getDeliveryMessage(),
                saveMemberOrderRequest.getOrderItems()
        );

        return ResponseEntity.status(HttpStatus.OK).body(new SimpleResponse("결제 완료"));
    }

    //멤버 주문 취소
    @PutMapping("/api/orders/member/cancel")
    public ResponseEntity<Object> cancelMemberOrder(@Valid @RequestBody CancelMemberOrderRequest cancelMemberOrderRequest, Principal principal) {

        StringBuilder message = new StringBuilder();

        //인증된 사람만 주문 취소가능
        if (principal == null || !StringUtils.hasText(principal.getName())) {
            message.append("액세스 토큰이 비었습니다. ");
            throw new MyBadRequestException(message.toString());
        }

        orderService.cancelMemberOrder(cancelMemberOrderRequest.getOrderId());
        return ResponseEntity.status(HttpStatus.OK).body(new SimpleResponse("결제 완료"));
    }

    //게스트 주문 저장
    @PostMapping("/api/orders/guest")
    public ResponseEntity<Object> saveGuestOrder(@Valid @RequestBody SaveGuestOrderRequest saveGuestOrderRequest, BindingResult bindingResult) {

        StringBuilder message = new StringBuilder();

        //결제는 memberOrderRequest 없어도 가능함. 근데 주문은 memberOrderRequest 없으면 결제 취소해야함
        if (bindingResult.hasFieldErrors()) {
            if (StringUtils.hasText(saveGuestOrderRequest.getPaymentId())) {
                orderService.cancelPortOnePayment(saveGuestOrderRequest.getPaymentId());
                message.append("결제 자동 취소! ");
            } else {
                String msg = "paymentId 누락으로 결제 취소 불가! ";
                message.append(msg);
                log.info(msg + saveGuestOrderRequest);
            }
            throw new MyBadRequestException(message.toString());
        }

        orderService.saveGuestOrder(
                saveGuestOrderRequest.getPaymentId(),
                saveGuestOrderRequest.getSenderNickname(),
                saveGuestOrderRequest.getSenderEmail(),
                saveGuestOrderRequest.getSenderPhone(),
                saveGuestOrderRequest.getReceiverNickname(),
                saveGuestOrderRequest.getReceiverPhone(),
                saveGuestOrderRequest.getZipCode(),
                saveGuestOrderRequest.getStreetAddress(),
                saveGuestOrderRequest.getDetailAddress(),
                saveGuestOrderRequest.getDeliveryMessage(),
                saveGuestOrderRequest.getOrderItems()
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
