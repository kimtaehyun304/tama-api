package org.example.tamaapi.domain.item;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tamaapi.domain.BaseEntity;
import org.example.tamaapi.domain.user.Member;
import org.example.tamaapi.domain.order.OrderItem;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false, unique = true)
    private OrderItem orderItem;

    // @ManyToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name = "item_stock_id", nullable = false)
    //private ColorItemSizeStock colorItemSizeStock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private int rating;

    @Column(nullable = false)
    private String comment;

    // 키는 달라질수 있다.
    private Integer height;

    // 몸무게는 달라질수 있다.
    private Integer weight;

    @Builder
    public Review(OrderItem orderItem, Member member, int rating, String comment, Integer height, Integer weight) {
        //this.colorItemSizeStock = colorItemSizeStock;
        this.orderItem = orderItem;
        this.member = member;
        this.rating = rating;
        this.comment = comment;
        this.height = height;
        this.weight = weight;
    }

}
