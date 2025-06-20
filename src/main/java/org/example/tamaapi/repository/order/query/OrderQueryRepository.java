package org.example.tamaapi.repository.order.query;


import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.example.tamaapi.domain.item.QReview;
import org.example.tamaapi.dto.requestDto.CustomPageRequest;
import org.example.tamaapi.dto.responseDto.CustomPage;
import org.example.tamaapi.dto.responseDto.order.AdminOrderResponse;
import org.example.tamaapi.dto.responseDto.order.QAdminOrderResponse;
import org.example.tamaapi.repository.item.ColorItemImageRepository;
import org.example.tamaapi.repository.order.query.dto.AdminOrderItemResponse;
import org.example.tamaapi.repository.order.query.dto.QAdminOrderItemResponse;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.example.tamaapi.domain.QMember.member;
import static org.example.tamaapi.domain.item.QColor.color;
import static org.example.tamaapi.domain.item.QColorItem.colorItem;
import static org.example.tamaapi.domain.item.QColorItemSizeStock.colorItemSizeStock;
import static org.example.tamaapi.domain.item.QItem.item;
import static org.example.tamaapi.domain.item.QReview.*;
import static org.example.tamaapi.domain.order.QDelivery.delivery;
import static org.example.tamaapi.domain.order.QOrder.*;
import static org.example.tamaapi.domain.order.QOrderItem.orderItem;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.util.StringUtils.hasText;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryRepository {

    private final EntityManager em;
    private final ColorItemImageRepository colorItemImageRepository;
    private final JPAQueryFactory queryFactory;

    public CustomPage<AdminOrderResponse> findAdminOrder(CustomPageRequest customPageRequest){
        CustomPage<AdminOrderResponse> customPage = findAdminOrdersWithPaging(customPageRequest);
        List<Long> orderIds = customPage.getContent().stream().map(AdminOrderResponse::getId).toList();
        Map<Long, List<AdminOrderItemResponse>> childrenMap = findAdminOrdersChildrenMap(orderIds);
        customPage.getContent().forEach(o -> o.setOrderItems(childrenMap.get(o.getId())));
        return customPage;
    }

    private CustomPage<AdminOrderResponse> findAdminOrdersWithPaging(CustomPageRequest customPageRequest){
        List<AdminOrderResponse> content = queryFactory.select(new QAdminOrderResponse(order, member.nickname)).from(order)
                .join(order.delivery, delivery).fetchJoin().join(order.member, member)
                .offset(customPageRequest.getPage() - 1)
                .limit(customPageRequest.getSize())
                .fetch();
        Long count = queryFactory.select(order.count()).from(order).fetchOne();
        return new CustomPage<>(content, customPageRequest, count);
    }

    private Map<Long, List<AdminOrderItemResponse>> findAdminOrdersChildrenMap(List<Long> orderIds){
        List<AdminOrderItemResponse> children = queryFactory
                .select(new QAdminOrderItemResponse(review.id.isNotNull(),orderItem.order.id, orderItem.id, orderItem.orderPrice, orderItem.count, item.name, colorItem.color.name, colorItemSizeStock.size)).from(orderItem)
                .leftJoin(review).on(review.orderItem.id.eq(orderItem.id))
                .join(orderItem.colorItemSizeStock, colorItemSizeStock).join(colorItemSizeStock.colorItem, colorItem).join(colorItem.color, color).join(colorItem.item, item)
                .where(orderItem.id.in(orderIds))
                .fetch();

       return children.stream().collect(Collectors.groupingBy(AdminOrderItemResponse::getOrderId));

    }

}
