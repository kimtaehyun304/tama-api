package org.example.tamaapi.domain.order;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tamaapi.domain.BaseEntity;
import org.example.tamaapi.domain.Guest;
import org.example.tamaapi.domain.Member;

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

    //비로그인 주문
    @Embedded
    private Guest guest;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    //cascade insert 여러번 나가서 jdbcTemplate 사용
    @OneToMany(mappedBy = "order")
    //@BatchSize(size = 1000)
    private final List<OrderItem> orderItems = new ArrayList<>();

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

    private Order(String paymentId, Member member, Delivery delivery, List<OrderItem> orderItems) {
        this.paymentId = paymentId;
        this.addMember(member);
        this.delivery = delivery;
        for (OrderItem orderItem : orderItems)
            this.addOrderItem(orderItem);
        this.status = OrderStatus.PAYMENT;
    }

    private Order(String paymentId, Guest guest, Delivery delivery, List<OrderItem> orderItems) {
        this.paymentId = paymentId;
        this.guest = guest;
        this.delivery = delivery;
        for (OrderItem orderItem : orderItems)
            this.addOrderItem(orderItem);
        this.status = OrderStatus.PAYMENT;
    }

    //==생성 메서드==//
    public static Order createMemberOrder(String paymentId, Member member, Delivery delivery, List<OrderItem> orderItems) {
       return new Order(paymentId,member,delivery,orderItems);
    }

    public static Order createGuestOrder(String paymentId, Guest guest, Delivery delivery, List<OrderItem> orderItems) {
        return new Order(paymentId,guest,delivery,orderItems);
    }

    public void cancelOrder(){
        status = OrderStatus.CANCEL;
    }

}

