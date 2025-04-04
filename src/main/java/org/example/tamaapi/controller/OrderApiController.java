package org.example.tamaapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.config.CustomUserDetails;
import org.example.tamaapi.config.aspect.PreAuthentication;
import org.example.tamaapi.domain.order.Order;
import org.example.tamaapi.dto.requestDto.CustomPageRequest;
import org.example.tamaapi.dto.requestDto.order.CancelMemberOrderRequest;
import org.example.tamaapi.dto.requestDto.order.SaveGuestOrderRequest;
import org.example.tamaapi.dto.requestDto.order.SaveMemberOrderRequest;

import org.example.tamaapi.dto.responseDto.CustomPage;
import org.example.tamaapi.dto.responseDto.SimpleResponse;
import org.example.tamaapi.dto.responseDto.order.AdminOrderResponse;
import org.example.tamaapi.dto.responseDto.order.OrderItemResponse;
import org.example.tamaapi.dto.responseDto.order.OrderResponse;
import org.example.tamaapi.dto.validator.PaymentValidator;
import org.example.tamaapi.exception.MyBadRequestException;
import org.example.tamaapi.repository.*;
import org.example.tamaapi.repository.item.ColorItemImageRepository;
import org.example.tamaapi.repository.item.ColorItemSizeStockRepository;
import org.example.tamaapi.repository.order.OrderItemRepository;
import org.example.tamaapi.repository.order.OrderRepository;
import org.example.tamaapi.service.EmailService;
import org.example.tamaapi.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.example.tamaapi.util.ErrorMessageUtil.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OrderApiController {

    private final ColorItemSizeStockRepository colorItemSizeStockRepository;
    private final PaymentValidator paymentValidator;
    private final OrderService orderService;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final EmailService emailService;
    private final ColorItemImageRepository colorItemImageRepository;
    private final OrderItemRepository orderItemRepository;

    //멤버 주문 조회
    @GetMapping("/api/orders/member")
    public CustomPage<OrderResponse> orders(@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @ModelAttribute CustomPageRequest customPageRequest) {
        if(userDetails == null)
            throw new IllegalArgumentException("액세스 토큰이 비었습니다.");

        //조회라 굳이 검증 안필요
        //Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_MEMBER));
        PageRequest pageRequest = PageRequest.of(customPageRequest.getPage() - 1, customPageRequest.getSize());
        Page<Order> orders = orderRepository.findAllWithMemberAndDeliveryByMemberId(userDetails.getId(), pageRequest);
        List<Long> orderIds = orders.stream().map(Order::getId).toList();

        Map<Long, List<OrderItemResponse>> orderItemsMap = orderItemRepository.findAllWithByOrderIdIn(orderIds).stream().map(OrderItemResponse::new).collect(Collectors.groupingBy(OrderItemResponse::getOrderId));
        List<OrderResponse> orderResponses = orders.stream().map(OrderResponse::new).toList();
        orderResponses.forEach(o -> o.setOrderItems(orderItemsMap.get(o.getId())));

        return new CustomPage<>(orderResponses, orders.getPageable(), orders.getTotalPages(), orders.getTotalElements());
    }

    //멤버 주문 저장
    @PostMapping("/api/orders/member")
    public ResponseEntity<Object> saveMemberOrder(@Valid @RequestBody SaveMemberOrderRequest saveMemberOrderRequest, BindingResult bindingResult, @AuthenticationPrincipal CustomUserDetails userDetails) {

        StringBuilder message = new StringBuilder();

        //밑에서 주문 취소
        if(userDetails == null)
            message.append("액세스 토큰이 비었습니다. ");

        //결제는 memberOrderRequest 없어도 가능함. 근데 주문은 memberOrderRequest 없으면 결제 취소해야함
        if (bindingResult.hasFieldErrors()) {
            for (FieldError fieldError : bindingResult.getFieldErrors())
                message.append(fieldError.getField()).append("는(은) ").append(fieldError.getDefaultMessage()).append(". ");
        }

        if (bindingResult.hasFieldErrors() || userDetails == null) {
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

        Long memberId = userDetails.getId();
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
    public ResponseEntity<Object> cancelMemberOrder(@Valid @RequestBody CancelMemberOrderRequest cancelMemberOrderRequest, @AuthenticationPrincipal CustomUserDetails userDetails) {

        if(userDetails == null)
            throw new MyBadRequestException("액세스 토큰이 비었습니다.");

        orderService.cancelMemberOrder(cancelMemberOrderRequest.getOrderId(), userDetails.getId());
        return ResponseEntity.status(HttpStatus.OK).body(new SimpleResponse("결제 취소 완료"));
    }

    //비로그인 주문 조회
    @GetMapping("/api/orders/guest")
    public OrderResponse guestOrder(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        // "Basic YWRtaW46cGFzc3dvcmQ=" 형태 → Base64 디코딩
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            throw new IllegalArgumentException(INVALID_HEADER);
        }

        String base64Credentials = authHeader.substring(6); // "Basic " 이후의 값 추출
        String decodedCredentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);

        // "orderId:buyerName" 형태에서 분리
        String[] values = decodedCredentials.split(":", 2);
        if (values.length != 2)
            throw new IllegalArgumentException(INVALID_HEADER);

        String buyerName = values[0];
        Long orderId = Long.parseLong(values[1]);
        Order order = orderRepository.findAllWithOrderItemAndDeliveryByOrderId(orderId).orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_ORDER));

        if(!order.getGuest().getNickname().equals(buyerName))
            throw new IllegalArgumentException(NOT_FOUND_ORDER);

       OrderResponse orderResponse = new OrderResponse(order);
       List<OrderItemResponse> orderItems = orderItemRepository.findAllWithByOrderIdIn(orderId).stream().map(OrderItemResponse::new).toList();
       orderResponse.setOrderItems(orderItems);

       return new OrderResponse(order);
    }

    //비로그인 주문 저장
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

        Long newOrderId = orderService.saveGuestOrder(
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

        try {
            emailService.sendGuestOrderEmail(saveGuestOrderRequest.getSenderEmail(), saveGuestOrderRequest.getSenderNickname(), newOrderId);
        } catch (Exception e) {
            orderService.cancelGuestOrder(newOrderId);
        }

        return ResponseEntity.status(HttpStatus.OK).body(new SimpleResponse("결제 완료"));
    }

    //게스트 주문 취소
    @PutMapping("/api/orders/guest/cancel")
    public ResponseEntity<Object> cancelGuestOrder(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {

        // "Basic YWRtaW46cGFzc3dvcmQ=" 형태 → Base64 디코딩
        if (authHeader == null || !authHeader.startsWith("Basic "))
            throw new IllegalArgumentException(INVALID_HEADER);

        String base64Credentials = authHeader.substring(6); // "Basic " 이후의 값 추출
        String decodedCredentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);

        // "orderId:buyerName" 형태에서 분리
        String[] values = decodedCredentials.split(":", 2);
        if (values.length != 2)
            throw new IllegalArgumentException(INVALID_HEADER);

        String buyerName = values[0];
        Long orderId = Long.parseLong(values[1]);

        Order order = orderRepository.findAllWithOrderItemAndDeliveryByOrderId(orderId).orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_ORDER));
        //사용자 검증
        if(!order.getGuest().getNickname().equals(buyerName))
            throw new IllegalArgumentException(NOT_FOUND_ORDER);

        orderService.cancelGuestOrder(orderId);
        return ResponseEntity.status(HttpStatus.OK).body(new SimpleResponse("결제 취소 완료"));
    }

    //localhost는 webhook 못씀
    //결제가 되면 포트원이 tama 엔드포인트 호출. 즉 리엑트에서 호출하는게 아니므로 포트원 결제 내역에 필요한 주문 정보를 다 저장해야함
    //webhook은 안전하지만 포트원에게 너무 많은 정보를 제공하는 것 같음. 팀원이랑 상의 필요
    @PostMapping("/api/orders/member/webhook")
    public void webhook() {
    }

    //모든 주문 조회
    @GetMapping("/api/orders")
    @PreAuthentication
    @PreAuthorize("hasRole('ADMIN')")
    public CustomPage<AdminOrderResponse> orders(@Valid @ModelAttribute CustomPageRequest customPageRequest) {
        PageRequest pageRequest = PageRequest.of(customPageRequest.getPage() - 1, customPageRequest.getSize());
        Page<Order> orders = orderRepository.findAllWithMemberAndDelivery(pageRequest);
        List<AdminOrderResponse> orderResponses = orders.stream().map(AdminOrderResponse::new).toList();
        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        Map<Long, List<OrderItemResponse>> orderItemsMap = orderItemRepository.findAllWithByOrderIdIn(orderIds).stream().map(OrderItemResponse::new).collect(Collectors.groupingBy(OrderItemResponse::getOrderId));
        orderResponses.forEach(o -> o.setOrderItems(orderItemsMap.get(o.getId())));

        return new CustomPage<>(orderResponses, orders.getPageable(), orders.getTotalPages(), orders.getTotalElements());
    }

}
