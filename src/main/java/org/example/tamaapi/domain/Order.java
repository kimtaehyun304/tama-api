package org.example.tamaapi.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
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

    //비로그인 주문
    @Embedded
    private Guest guest;


    //@ManyToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name = "guest_id")
    //private Guest guest;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    //cascade insert 여러번 나가서 jdbcTemplate 사용
    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems = new ArrayList<>();

    //포트원 결제 번호 (문자열)
    private String paymentId;

    //==연관관계 메서드==//
    public void addMember(Member member) {
        this.member = member;
        member.getOrders().add(this);
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    //==생성 메서드==//
    public static Order createMemberOrder(String paymentId, Member member, Delivery delivery, List<OrderItem> orderItems) {
        Order order = new Order();
        order.setPaymentId(paymentId);
        order.addMember(member);
        order.setDelivery(delivery);
        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        order.setStatus(OrderStatus.ORDER);
        return order;
    }

    public static Order createGuestOrder(String paymentId, Guest guest, Delivery delivery, List<OrderItem> orderItems) {
        Order order = new Order();
        order.setPaymentId(paymentId);
        order.setGuest(guest);
        order.setDelivery(delivery);
        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        order.setStatus(OrderStatus.ORDER);
        return order;
    }

}

