package org.example.tamaapi.repository.order.query;


import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.example.tamaapi.domain.item.Category;
import org.example.tamaapi.domain.item.ColorItemImage;
import org.example.tamaapi.domain.item.QCategory;
import org.example.tamaapi.dto.UploadFile;
import org.example.tamaapi.dto.requestDto.CustomPageRequest;
import org.example.tamaapi.dto.responseDto.CustomPage;
import org.example.tamaapi.repository.MemberCouponRepository;
import org.example.tamaapi.repository.item.CategoryRepository;
import org.example.tamaapi.repository.order.query.dto.*;
import org.example.tamaapi.repository.item.ColorItemImageRepository;
import org.example.tamaapi.service.OrderService;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import static java.time.LocalTime.now;
import static org.example.tamaapi.domain.item.QCategory.category;
import static org.example.tamaapi.domain.item.QColor.color;
import static org.example.tamaapi.domain.item.QColorItem.colorItem;
import static org.example.tamaapi.domain.item.QColorItemSizeStock.colorItemSizeStock;
import static org.example.tamaapi.domain.item.QItem.item;
import static org.example.tamaapi.domain.item.QReview.*;
import static org.example.tamaapi.domain.order.QDelivery.delivery;
import static org.example.tamaapi.domain.order.QOrder.*;
import static org.example.tamaapi.domain.order.QOrderItem.orderItem;
import static org.example.tamaapi.domain.user.QMember.member;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryRepository {

    private final ColorItemImageRepository colorItemImageRepository;
    private final JPAQueryFactory queryFactory;
    private final OrderService orderService;
    private final MemberCouponRepository memberCouponRepository;
    private final EntityManager em;
    private final CategoryRepository categoryRepository;

    //★주문 조회 자식 컬렉션(공용) & 멤버 주문 조회 때문에 리뷰 조인
    private Map<Long, List<OrderItemResponse>> findOrdersChildrenMap(List<Long> orderIds) {
        List<OrderItemResponse> children = queryFactory
                .select(new QOrderItemResponse(orderItem.order.id, colorItem.id, orderItem.id, orderItem.orderPrice,
                        orderItem.count, item.name, colorItem.color.name, colorItemSizeStock.size, review.id.isNotNull()))
                .from(orderItem).leftJoin(review).on(orderItem.id.eq(review.orderItem.id))
                .join(orderItem.colorItemSizeStock, colorItemSizeStock).join(colorItemSizeStock.colorItem, colorItem).join(colorItem.color, color).join(colorItem.item, item)
                .where(orderItem.order.id.in(orderIds))
                .fetch();

        List<Long> colorItemIds = children.stream().map(OrderItemResponse::getColorItemId).toList();
        List<ColorItemImage> images = colorItemImageRepository.findAllByColorItemIdInAndSequence(colorItemIds, 1);
        Map<Long, UploadFile> uploadFileMap = images.stream().collect(Collectors.toMap(c -> c.getColorItem().getId(), ColorItemImage::getUploadFile));

        children.forEach(child -> child.setUploadFile(
                uploadFileMap.get(child.getColorItemId())
        ));

        return children.stream().collect(Collectors.groupingBy(OrderItemResponse::getOrderId));
    }


    //★멤버 주문 조회
    public CustomPage<MemberOrderResponse> findMemberOrdersWithPaging(CustomPageRequest customPageRequest, Long memberId) {
        List<MemberOrderResponse> content = queryFactory.select(new QMemberOrderResponse(order))
                .from(order).join(order.delivery, delivery).fetchJoin()
                .where(order.member.id.eq(memberId))
                .offset((long) (customPageRequest.getPage() - 1) * customPageRequest.getSize())
                .limit(customPageRequest.getSize())
                .orderBy(new OrderSpecifier<>(Order.DESC, order.id))
                .fetch();

        Long count = queryFactory.select(order.count()).from(order).where(order.member.id.eq(memberId)).fetchOne();

        List<Long> orderIds = content.stream().map(MemberOrderResponse::getId).toList();
        Map<Long, List<OrderItemResponse>> childrenMap = findOrdersChildrenMap(orderIds);


        content.forEach(o -> o.setOrderItems(childrenMap.get(o.getId())));
        return new CustomPage<>(content, customPageRequest, count);
    }


    public Optional<GuestOrderResponse> findGuestOrder(Long orderId) {
        GuestOrderResponse guestOrderResponse = queryFactory.select(new QGuestOrderResponse(order))
                .from(order).join(order.delivery, delivery).fetchJoin().join(order.orderItems, orderItem).fetchJoin()
                .where(order.id.eq(orderId))
                .fetchOne();

        Map<Long, List<OrderItemResponse>> childrenMap = findOrdersChildrenMap(List.of(guestOrderResponse.getId()));
        guestOrderResponse.setOrderItems(childrenMap.get(orderId));
        return Optional.of(guestOrderResponse);
    }

    public CustomPage<AdminOrderResponse> findAdminOrdersWithPaging(CustomPageRequest customPageRequest) {
        List<AdminOrderResponse> content = queryFactory.select(new QAdminOrderResponse(order, member.nickname)).from(order)
                .join(order.delivery, delivery).fetchJoin().join(order.member, member)
                .offset((long) (customPageRequest.getPage() - 1) * customPageRequest.getSize())
                .limit(customPageRequest.getSize())
                .orderBy(new OrderSpecifier<>(Order.DESC, order.id))
                .fetch();

        Long count = queryFactory.select(order.count()).from(order).fetchOne();

        List<Long> orderIds = content.stream().map(AdminOrderResponse::getId).toList();
        Map<Long, List<OrderItemResponse>> childrenMap = findOrdersChildrenMap(orderIds);

        content.forEach(o -> o.setOrderItems(childrenMap.get(o.getId())));
        return new CustomPage<>(content, customPageRequest, count);
    }

    //-------------------------------------------------------------------------------------------------------------------------------

    public AdminSalesResponse findAdminSales(YearMonth yearMonth) {
        //2026-01-01 00:00:00

        // 해당 월 1일
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        // 해당 월 마지막 날
        LocalDateTime end = start.plusMonths(1);
        //LocalDateTime start = LocalDate.now().minusMonths(1).withDayOfMonth(1).atStartOfDay();
        //LocalDateTime start = LocalDate.of(2023, 1, 1).atStartOfDay();
        //LocalDateTime end = start.plusMonths(1);

        //DATE()는 java.util.date를 반환해서 이렇게 해야함
        DateTemplate<Date> today = Expressions.dateTemplate(Date.class, "DATE({0})", order.createdAt);
        //이렇게하면 argument mismatch
        //DateTemplate<LocalDate> today = Expressions.dateTemplate(LocalDate.class, "DATE({0})", order.createdAt);

        //이번달의 날마다 매출, 주문수
        List<AdminMonthSalesResponse> monthSales = queryFactory
                .select(new QAdminMonthSalesResponse(today, order.count(), orderItem.orderPrice.subtract(order.usedCouponPrice).subtract(order.usedPoint).sum()))
                .from(order).join(order.orderItems, orderItem)
                .where(order.createdAt.goe(start), order.createdAt.lt(end))
                .groupBy(today)
                .orderBy(new OrderSpecifier<>(Order.ASC, today))
                .fetch();

        //부모 카테고리별 매출
        QCategory parent = new QCategory("parent"); // alias, category.parent를 나타냄
        List<AdminCategorySalesResponse> parentCategorySales = queryFactory
                .select(new QAdminCategorySalesResponse(parent.name.coalesce(category.name), order.count(), orderItem.orderPrice.subtract(order.usedCouponPrice).subtract(order.usedPoint).sum()))
                .from(orderItem).join(orderItem.colorItemSizeStock, colorItemSizeStock).join(colorItemSizeStock.colorItem, colorItem).join(colorItem.item, item)
                .join(item.category, category).leftJoin(category.parent, parent)
                .join(orderItem.order, order)
                .where(order.createdAt.goe(start), order.createdAt.lt(end))
                .groupBy(parent.id.coalesce(category.id), parent.name.coalesce(category.name))
                .fetch();

        //자식 카테고리별 매출
        List<ChildCategorySalesResponse> children = queryFactory
                .select(new QChildCategorySalesResponse(parent.name, category.name, order.count(), orderItem.orderPrice.subtract(order.usedCouponPrice).subtract(order.usedPoint).sum()))
                .from(orderItem).join(orderItem.colorItemSizeStock, colorItemSizeStock).join(colorItemSizeStock.colorItem, colorItem).join(colorItem.item, item)
                .join(item.category, category).leftJoin(category.parent, parent)
                .join(orderItem.order, order)
                .where(order.createdAt.goe(start), order.createdAt.lt(end))
                .groupBy(category.id)
                .fetch();

        //parent categoryName 으로 그루핑
        //자식 카테고리로 설정하기 애매한 경우엔 그냥 부모 카테고리로 설정한 경우가 있어서
        Map<String, List<ChildCategorySalesResponse>> groupingMap = children.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getParentName() != null ? c.getParentName() : c.getCategoryName()
                ));

        for (AdminCategorySalesResponse parentCategorySale : parentCategorySales)
            parentCategorySale.setChildren(groupingMap.get(parentCategorySale.getCategoryName()));

        return new AdminSalesResponse(monthSales, parentCategorySales);
    }
}
