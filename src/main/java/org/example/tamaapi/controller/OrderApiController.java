package org.example.tamaapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.config.CustomPrincipal;
import org.example.tamaapi.config.aspect.PreAuthentication;
import org.example.tamaapi.domain.order.PortOnePaymentStatus;
import org.example.tamaapi.domain.order.Order;
import org.example.tamaapi.dto.requestDto.CustomPageRequest;
import org.example.tamaapi.dto.requestDto.order.*;
import org.example.tamaapi.dto.responseDto.CustomPage;
import org.example.tamaapi.dto.responseDto.SimpleResponse;
import org.example.tamaapi.repository.order.query.dto.GuestOrderResponse;
import org.example.tamaapi.repository.order.query.dto.MemberOrderResponse;
import org.example.tamaapi.exception.MyBadRequestException;
import org.example.tamaapi.repository.order.OrderItemRepository;
import org.example.tamaapi.repository.order.OrderRepository;
import org.example.tamaapi.repository.order.query.dto.AdminOrderResponse;
import org.example.tamaapi.repository.order.query.OrderQueryRepository;
import org.example.tamaapi.service.EmailService;
import org.example.tamaapi.service.OrderService;
import org.example.tamaapi.service.PortOneService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static org.example.tamaapi.util.ErrorMessageUtil.INVALID_HEADER;
import static org.example.tamaapi.util.ErrorMessageUtil.NOT_FOUND_ORDER;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OrderApiController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final EmailService emailService;
    private final OrderQueryRepository orderQueryRepository;
    private final PortOneService portOneService;

    //멤버 주문 조회
    @GetMapping("/api/orders/member")
    public CustomPage<MemberOrderResponse> orders(@AuthenticationPrincipal CustomPrincipal principal, @Valid @ModelAttribute CustomPageRequest customPageRequest) {
        if(principal == null)
            throw new IllegalArgumentException("액세스 토큰이 비었습니다.");

        //조회라 굳이 멤버 존재 체크 안필요
        return orderQueryRepository.findMemberOrdersWithPaging(customPageRequest, principal.getMemberId());
    }

    //멤버 주문 저장
    @PostMapping("/api/orders/member")
    public ResponseEntity<SimpleResponse> saveMemberOrder(@RequestParam String paymentId, @AuthenticationPrincipal CustomPrincipal principal) {
        Map<String, Object> paymentResponse = portOneService.findByPaymentId(paymentId);
        SaveOrderRequest saveOrderRequest = portOneService.extractCustomData((String) paymentResponse.get("customData"));

        if (principal == null) {
            String cancelMsg = (paymentId == null)
                    ? "구매자 PK 누락되어 주문 거절. paymentId 누락 되어 결제 취소 불가"
                    : "구매자 PK 누락되어 주문 거절. 결제 취소";

            if (paymentId == null)
                log.error("[{}] {}", cancelMsg, saveOrderRequest);
            else
                portOneService.cancelPayment(paymentId, cancelMsg);

            throw new IllegalArgumentException(cancelMsg);
        }

        orderService.validateSaveOrderRequest(saveOrderRequest);

        Long memberId = principal.getMemberId();

        orderService.saveMemberOrder(
                saveOrderRequest.getPaymentId(),
                memberId,
                saveOrderRequest.getReceiverNickname(),
                saveOrderRequest.getReceiverPhone(),
                saveOrderRequest.getZipCode(),
                saveOrderRequest.getStreetAddress(),
                saveOrderRequest.getDetailAddress(),
                saveOrderRequest.getDeliveryMessage(),
                saveOrderRequest.getOrderItems()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(new SimpleResponse("결제 완료"));
    }

    //멤버 주문 저장
    @PostMapping("/api/orders/member/mobile")
    public ResponseEntity<SimpleResponse> saveMemberOrderMobile(@RequestParam String paymentId, @AuthenticationPrincipal CustomPrincipal principal) {
        Map<String, Object> paymentResponse = portOneService.findByPaymentId(paymentId);
        PortOnePaymentStatus portOnePaymentStatus = PortOnePaymentStatus.valueOf((String) paymentResponse.get("status"));

        //모바일 결제는 브라우저에서 바로 결재하는거라 응답 확인 불가
        if(portOnePaymentStatus.equals(PortOnePaymentStatus.FAILED))
            throw new IllegalArgumentException("포트원 결제 실패로 인한 주문 진행 불가");

        SaveOrderRequest saveOrderRequest = portOneService.extractCustomData((String) paymentResponse.get("customData"));

        if (principal == null) {
            String cancelMsg = (paymentId == null)
                    ? "구매자 PK 누락되어 주문 거절. paymentId 누락 되어 결제 취소 불가"
                    : "구매자 PK 누락되어 주문 거절. 결제 취소";

            if (paymentId == null)
                log.error("[{}] {}", cancelMsg, saveOrderRequest);
            else
                portOneService.cancelPayment(paymentId, cancelMsg);

            throw new IllegalArgumentException(cancelMsg);
        }

        orderService.validateSaveOrderRequest(saveOrderRequest);

        Long memberId = principal.getMemberId();
        orderService.saveMemberOrder(
                saveOrderRequest.getPaymentId(),
                memberId,
                saveOrderRequest.getReceiverNickname(),
                saveOrderRequest.getReceiverPhone(),
                saveOrderRequest.getZipCode(),
                saveOrderRequest.getStreetAddress(),
                saveOrderRequest.getDetailAddress(),
                saveOrderRequest.getDeliveryMessage(),
                saveOrderRequest.getOrderItems()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(new SimpleResponse("결제 완료"));
    }


    //멤버 주문 취소
    @PutMapping("/api/orders/member/cancel")
    public ResponseEntity<SimpleResponse> cancelMemberOrder(@Valid @RequestBody CancelMemberOrderRequest cancelMemberOrderRequest, @AuthenticationPrincipal CustomPrincipal principal) {

        if(principal == null)
            throw new MyBadRequestException("액세스 토큰이 비었습니다.");

        orderService.cancelMemberOrder(cancelMemberOrderRequest.getOrderId(), principal.getMemberId(), "구매자 취소 요청");
        return ResponseEntity.status(HttpStatus.OK).body(new SimpleResponse("결제 취소 완료"));
    }

    //비로그인 주문 조회
    @GetMapping("/api/orders/guest")
    public GuestOrderResponse guestOrder(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
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

        GuestOrderResponse guestOrderResponse = orderQueryRepository.findGuestOrder(orderId).orElseThrow(() -> new IllegalArgumentException(NOT_FOUND_ORDER));

        if(!guestOrderResponse.getGuestName().equals(buyerName))
            throw new IllegalArgumentException(NOT_FOUND_ORDER);

        return guestOrderResponse;
    }

    //비로그인 주문 저장
    @PostMapping("/api/orders/guest")
    public ResponseEntity<SimpleResponse> saveGuestOrder(@RequestParam String paymentId) {
        Map<String, Object> paymentResponse = portOneService.findByPaymentId(paymentId);
        SaveOrderRequest saveOrderRequest = portOneService.extractCustomData((String) paymentResponse.get("customData"));

        orderService.validateSaveOrderRequest(saveOrderRequest);

        Long newOrderId = orderService.saveGuestOrder(
                saveOrderRequest.getPaymentId(),
                saveOrderRequest.getSenderNickname(),
                saveOrderRequest.getSenderEmail(),
                saveOrderRequest.getReceiverNickname(),
                saveOrderRequest.getReceiverPhone(),
                saveOrderRequest.getZipCode(),
                saveOrderRequest.getStreetAddress(),
                saveOrderRequest.getDetailAddress(),
                saveOrderRequest.getDeliveryMessage(),
                saveOrderRequest.getOrderItems()
        );

        try {
            emailService.sendGuestOrderEmail(saveOrderRequest.getSenderEmail(), saveOrderRequest.getSenderNickname(), newOrderId);
        } catch (Exception e) {
            orderService.cancelGuestOrder(newOrderId, "주문 내역 메일 발송 실패로인한 결제 취소");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(new SimpleResponse("결제 완료"));
    }

    //비로그인 주문 저장
    @PostMapping("/api/orders/guest/mobile")
    public ResponseEntity<SimpleResponse> saveGuestOrderMobile(@RequestParam String paymentId) {
        Map<String, Object> paymentResponse = portOneService.findByPaymentId(paymentId);
        PortOnePaymentStatus portOnePaymentStatus = PortOnePaymentStatus.valueOf((String) paymentResponse.get("status"));

        if(portOnePaymentStatus.equals(PortOnePaymentStatus.FAILED))
            throw new IllegalArgumentException("포트원 결제 실패로 인한 주문 진행 불가");

        SaveOrderRequest saveOrderRequest = portOneService.extractCustomData((String) paymentResponse.get("customData"));
        orderService.validateSaveOrderRequest(saveOrderRequest);

        Long newOrderId = orderService.saveGuestOrder(
                saveOrderRequest.getPaymentId(),
                saveOrderRequest.getSenderNickname(),
                saveOrderRequest.getSenderEmail(),
                saveOrderRequest.getReceiverNickname(),
                saveOrderRequest.getReceiverPhone(),
                saveOrderRequest.getZipCode(),
                saveOrderRequest.getStreetAddress(),
                saveOrderRequest.getDetailAddress(),
                saveOrderRequest.getDeliveryMessage(),
                saveOrderRequest.getOrderItems()
        );

        try {
            emailService.sendGuestOrderEmail(saveOrderRequest.getSenderEmail(), saveOrderRequest.getSenderNickname(), newOrderId);
        } catch (Exception e) {
            orderService.cancelGuestOrder(newOrderId, "주문 내역 메일 발송 실패로인한 결제 취소");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(new SimpleResponse("결제 완료"));
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

        orderService.cancelGuestOrder(orderId, "구매자 취소 요청");
        return ResponseEntity.status(HttpStatus.OK).body(new SimpleResponse("결제 취소 완료"));
    }

    //localhost는 webhook 못씀
    //결제가 되면 포트원이 tama 엔드포인트 호출. 즉 리엑트에서 호출하는게 아니므로 포트원 결제 내역에 필요한 주문 정보를 다 저장해야함
    //webhook은 통신 질이 좋아지지만, 포트원이 DB 수준으로 정보를 갖게 됨 -> 팀원이랑 상의 필요
    //모바일 결제는 리다이렉트 방식이라 webhook처럼 포트원에 정보를 저장해야함
    //webhook url은 하나만 가능, 로직 완료시 클라이언트 응답 불가 -> 웹훅 포기
    @PostMapping("/api/webhook/portOne")
    public void webhook() {

    }

    //모든 주문 조회
    @GetMapping("/api/orders")
    @PreAuthentication
    @PreAuthorize("hasRole('ADMIN')")
    public CustomPage<AdminOrderResponse> orders(@Valid @ModelAttribute CustomPageRequest customPageRequest) {
        return orderQueryRepository.findAdminOrdersWithPaging(customPageRequest);
    }

}
