package org.example.tamaapi.repository.order.query;


import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.tamaapi.domain.item.ColorItemImage;
import org.example.tamaapi.dto.UploadFile;
import org.example.tamaapi.dto.requestDto.CustomPageRequest;
import org.example.tamaapi.dto.responseDto.CustomPage;
import org.example.tamaapi.repository.MemberCouponRepository;
import org.example.tamaapi.repository.order.query.dto.*;
import org.example.tamaapi.repository.item.ColorItemImageRepository;
import org.example.tamaapi.service.OrderService;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


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

    //★주문 조회 자식 컬렉션(공용) & 멤버 주문 조회 때문에 리뷰 조인
    private Map<Long, List<OrderItemResponse>> findOrdersChildrenMap(List<Long> orderIds){
        List<OrderItemResponse> children = queryFactory
                .select(new QOrderItemResponse(orderItem.order.id, colorItem.id, orderItem.id, orderItem.orderPrice, orderItem.count, item.name, colorItem.color.name, colorItemSizeStock.size, review.id.isNotNull()))
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

    //-------------------------------------------------------------------------------------------------------------------------------
    //★멤버 주문 조회
    public CustomPage<MemberOrderResponse> findMemberOrdersWithPaging(CustomPageRequest customPageRequest, Long memberId){
        List<MemberOrderResponse> content = queryFactory.select(new QMemberOrderResponse(order))
                .from(order).join(order.delivery, delivery).fetchJoin()
                .where(order.member.id.eq(memberId))
                .offset((long) (customPageRequest.getPage() - 1) *customPageRequest.getSize())
                .limit(customPageRequest.getSize())
                .orderBy(new OrderSpecifier<>(Order.DESC, order.id))
                .fetch();

        Long count = queryFactory.select(order.count()).from(order).where(order.member.id.eq(memberId)).fetchOne();

        List<Long> orderIds = content.stream().map(MemberOrderResponse::getId).toList();
        Map<Long, List<OrderItemResponse>> childrenMap = findOrdersChildrenMap(orderIds);


        content.forEach(o -> o.setOrderItems(childrenMap.get(o.getId())));
        return new CustomPage<>(content, customPageRequest, count);
    }

    //-------------------------------------------------------------------------------------------------------------------------------
    public Optional<GuestOrderResponse> findGuestOrder(Long orderId){
        GuestOrderResponse guestOrderResponse = queryFactory.select(new QGuestOrderResponse(order))
                .from(order).join(order.delivery, delivery).fetchJoin().join(order.orderItems, orderItem).fetchJoin()
                .where(order.id.eq(orderId))
                .fetchOne();

        Map<Long, List<OrderItemResponse>> childrenMap = findOrdersChildrenMap(List.of(guestOrderResponse.getId()));
        guestOrderResponse.setOrderItems(childrenMap.get(orderId));
        return Optional.of(guestOrderResponse);
    }
    //-------------------------------------------------------------------------------------------------------------------------------
    public CustomPage<AdminOrderResponse> findAdminOrdersWithPaging(CustomPageRequest customPageRequest){
        List<AdminOrderResponse> content = queryFactory.select(new QAdminOrderResponse(order, member.nickname)).from(order)
                .join(order.delivery, delivery).fetchJoin().join(order.member, member)
                .offset((long) (customPageRequest.getPage() - 1) *customPageRequest.getSize())
                .limit(customPageRequest.getSize())
                .orderBy(new OrderSpecifier<>(Order.DESC, order.id))
                .fetch();

        Long count = queryFactory.select(order.count()).from(order).fetchOne();

        List<Long> orderIds = content.stream().map(AdminOrderResponse::getId).toList();
        Map<Long, List<OrderItemResponse>> childrenMap = findOrdersChildrenMap(orderIds);

        content.forEach(o -> o.setOrderItems(childrenMap.get(o.getId())));
        return new CustomPage<>(content, customPageRequest, count);
    }

}
