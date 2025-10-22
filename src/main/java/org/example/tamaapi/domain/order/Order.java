package org.example.tamaapi.domain.order;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tamaapi.domain.BaseEntity;
import org.example.tamaapi.domain.user.coupon.MemberCoupon;
import org.example.tamaapi.domain.user.Guest;
import org.example.tamaapi.domain.user.Member;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Embedded
    private Guest guest;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "member_coupon_id", unique = true)
    private MemberCoupon memberCoupon;

    //매번 계산할 수 있지만 코드가 복잡해져서 필드 만듬
    //할인 금액이 의미있는 정보이기도 함
    private int usedCouponPrice;

    private int usedPoint;

    //배송비는 달라질수 있음. 또한 얼마 이상 구매시 무료가 되는 기준 금액도 달라질수 있음
    private int shippingFee;

    //포트원 결제 번호 (문자열)
    private String paymentId;

    //cascade는 insert 여러번 실행되서 jdbcTemplate 사용
    @OneToMany(mappedBy = "order")
    //@BatchSize(size = 1000) osiv off
    private List<OrderItem> orderItems = new ArrayList<>();


    //==연관관계 메서드==//
    public void addMember(Member member) {
        this.member = member;
        member.getOrders().add(this);
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    private Order(String paymentId, Member member, Delivery delivery, MemberCoupon memberCoupon, int usedCouponPrice, int usedPoint, int shippingFee, List<OrderItem> orderItems) {
        this.paymentId = paymentId;
        this.addMember(member);
        this.delivery = delivery;
        this.usedCouponPrice = usedCouponPrice;
        this.usedPoint = usedPoint;
        this.shippingFee = shippingFee;
        this.status = OrderStatus.ORDER_RECEIVED;
        for (OrderItem orderItem : orderItems)
            this.addOrderItem(orderItem);
        this.memberCoupon = memberCoupon;
    }

    private Order(String paymentId, Guest guest, Delivery delivery, int shippingFee, List<OrderItem> orderItems) {
        this.paymentId = paymentId;
        this.guest = guest;
        this.delivery = delivery;
        this.shippingFee = shippingFee;
        this.status = OrderStatus.ORDER_RECEIVED;
        for (OrderItem orderItem : orderItems)
            this.addOrderItem(orderItem);
    }

    //==생성 메서드==//
    public static Order createMemberOrder(String paymentId, Member member, Delivery delivery, MemberCoupon memberCoupon, int usedCouponPrice, int usedPoint, int shippingFee, List<OrderItem> orderItems) {
        return new Order(paymentId, member, delivery, memberCoupon, usedCouponPrice, usedPoint, shippingFee, orderItems);
    }

    public static Order createGuestOrder(String paymentId, Guest guest, Delivery delivery, int shippingFee, List<OrderItem> orderItems) {
        return new Order(paymentId, guest, delivery, shippingFee, orderItems);
    }

    public void cancelOrder() {
        status = OrderStatus.CANCEL_RECEIVED;
    }

    public void setIdByBatchId(Long id) {
        this.id = id;
    }

    public void changeStatus(OrderStatus status) {
        this.status = status;
    }

}

