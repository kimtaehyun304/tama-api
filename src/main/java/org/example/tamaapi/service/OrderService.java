package org.example.tamaapi.service;

import lombok.RequiredArgsConstructor;
import org.example.tamaapi.domain.Delivery;
import org.example.tamaapi.domain.Member;
import org.example.tamaapi.domain.Order;
import org.example.tamaapi.domain.OrderItem;
import org.example.tamaapi.domain.item.ColorItemSizeStock;
import org.example.tamaapi.domain.item.Review;
import org.example.tamaapi.dto.requestDto.OrderItemRequest;
import org.example.tamaapi.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final OrderItemRepository orderItemRepository;
    private final ColorItemSizeStockRepository colorItemSizeStockRepository;
    private final DeliveryRepository deliveryRepository;

    public void saveMemberOrder(Long memberId,
                                String receiverNickname,
                                String receiverPhone,
                                String zipCode,
                                String streetAddress,
                                String detailAddress,
                                String message,
                                List<OrderItemRequest> orderItemRequests) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("등록되지 않은 회원입니다."));
        Delivery delivery = new Delivery(zipCode, streetAddress, detailAddress, message, receiverNickname, receiverPhone);

        //성능 최적화를 위한 map
        List<Long> colorItemSizeStockIds = orderItemRequests.stream().map(OrderItemRequest::getColorItemSizeStockId).toList();
        List<ColorItemSizeStock> colorItemSizeStocks = colorItemSizeStockRepository.findAllWithColorItemAndItemByIdIn(colorItemSizeStockIds);

        Map<Long, ColorItemSizeStock> colorItemSizeStockMap = new HashMap<>();
        for (ColorItemSizeStock colorItemSizeStock : colorItemSizeStocks)
            colorItemSizeStockMap.put(colorItemSizeStock.getId(), colorItemSizeStock);

        //객체 주소값 할당
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequest orderItemRequest : orderItemRequests) {
            ColorItemSizeStock colorItemSizeStock = colorItemSizeStockMap.get(orderItemRequest.getColorItemSizeStockId());
            OrderItem orderItem = OrderItem.builder().colorItemSizeStock(colorItemSizeStock).count(orderItemRequest.getOrderCount()).build();
            orderItems.add(orderItem);
        }
        Order order = Order.createOrder(member, delivery, orderItems);
        orderRepository.save(order);
    }
}
