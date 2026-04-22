package org.example.tamaapi.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.domain.item.ColorItemSizeStock;
import org.example.tamaapi.domain.order.*;
import org.example.tamaapi.domain.user.Authority;
import org.example.tamaapi.domain.user.Guest;
import org.example.tamaapi.domain.user.Member;
import org.example.tamaapi.domain.user.MemberAddress;
import org.example.tamaapi.domain.user.coupon.CouponType;
import org.example.tamaapi.domain.user.coupon.MemberCoupon;
import org.example.tamaapi.dto.PortOneOrder;
import org.example.tamaapi.dto.requestDto.order.OrderItemRequest;
import org.example.tamaapi.dto.requestDto.order.OrderRequest;
import org.example.tamaapi.dto.responseDto.sweetTracker.DeliveryTrackingResponse;
import org.example.tamaapi.exception.OrderFailException;
import org.example.tamaapi.exception.UsedPaymentIdException;
import org.example.tamaapi.exception.WillCancelPaymentException;
import org.example.tamaapi.repository.JdbcTemplateRepository;
import org.example.tamaapi.repository.MemberAddressRepository;
import org.example.tamaapi.repository.MemberCouponRepository;
import org.example.tamaapi.repository.MemberRepository;
import org.example.tamaapi.repository.item.ColorItemSizeStockRepository;
import org.example.tamaapi.repository.order.DeliveryRepository;
import org.example.tamaapi.repository.order.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.*;

import static org.example.tamaapi.util.ErrorMessageUtil.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final SweetTrackerService sweetTrackerService;

    /*
    public DeliveryTrackingResponse findTrackingInfo(){

    }
    */
}