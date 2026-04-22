package org.example.tamaapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.aspect.PreAuthentication;
import org.example.tamaapi.domain.order.Courier;
import org.example.tamaapi.domain.order.PortOnePaymentStatus;
import org.example.tamaapi.dto.PortOneOrder;
import org.example.tamaapi.dto.requestDto.CustomPageRequest;
import org.example.tamaapi.dto.requestDto.SweetTrackerRequest;
import org.example.tamaapi.dto.requestDto.order.CancelMemberOrderRequest;
import org.example.tamaapi.dto.requestDto.order.DeliveryTrackingRequest;
import org.example.tamaapi.dto.requestDto.order.OrderRequest;
import org.example.tamaapi.dto.responseDto.CustomPage;
import org.example.tamaapi.dto.responseDto.SimpleResponse;
import org.example.tamaapi.dto.responseDto.review.CourierResponse;
import org.example.tamaapi.dto.responseDto.sweetTracker.DeliveryTrackingResponse;
import org.example.tamaapi.exception.MyBadRequestException;
import org.example.tamaapi.repository.MemberCouponRepository;
import org.example.tamaapi.repository.MemberRepository;
import org.example.tamaapi.repository.order.OrderRepository;
import org.example.tamaapi.repository.order.query.OrderQueryRepository;
import org.example.tamaapi.repository.order.query.dto.AdminOrderResponse;
import org.example.tamaapi.repository.order.query.dto.AdminSalesResponse;
import org.example.tamaapi.repository.order.query.dto.GuestOrderResponse;
import org.example.tamaapi.repository.order.query.dto.MemberOrderResponse;
import org.example.tamaapi.service.EmailService;
import org.example.tamaapi.service.OrderService;
import org.example.tamaapi.service.PortOneService;
import org.example.tamaapi.service.SweetTrackerService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.example.tamaapi.util.ErrorMessageUtil.INVALID_HEADER;
import static org.example.tamaapi.util.ErrorMessageUtil.NOT_FOUND_ORDER;

@RestController
@RequiredArgsConstructor
@Slf4j
public class DeliveryApiController {

    private final OrderService orderService;
    private final SweetTrackerService sweetTrackerService;

    //컬럼값 변경이라 put 사용
    @PutMapping("/api/delivery/{deliveryId}/tracking")
    @PreAuthentication
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SimpleResponse> changeTracking(@PathVariable Long deliveryId, @Valid @RequestBody DeliveryTrackingRequest req, @AuthenticationPrincipal Long memberId) {
        if (memberId == null)
            throw new MyBadRequestException("액세스 토큰이 비었습니다.");

        orderService.changeDeliveryTracking(deliveryId, req.getCourier(), req.getTrackingNumber());
        return ResponseEntity.status(HttpStatus.OK).body(new SimpleResponse("운송장 등록 완료"));
    }

    @GetMapping("/api/delivery/courier/available")
    public List<CourierResponse> availableList() {
        return Arrays.stream(Courier.values())
                .map(c -> new CourierResponse(c.getCode(),c.getKor(), c.name()))
                .toList();
    }

    @GetMapping("/api/delivery/tracking")
    public DeliveryTrackingResponse tracking(@Valid @ModelAttribute SweetTrackerRequest req) {
        return sweetTrackerService.findTrackingInfo(req.getCourier(), req.getTrackingNumber());
    }

}