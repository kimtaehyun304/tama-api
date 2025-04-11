package org.example.tamaapi.domain.order;

import jakarta.persistence.*;
import lombok.*;
import org.example.tamaapi.domain.item.ColorItemSizeStock;
import org.example.tamaapi.domain.item.Review;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "orderItem")
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "color_item_size_stock_id")
    private ColorItemSizeStock colorItemSizeStock;

    //구매 후 가격 변동 or 할인 쿠폰 고려
    private int orderPrice;

    private int count;

    //setOrder는 createOrder에서 연관메서드로 함
    @Builder
    public OrderItem(ColorItemSizeStock colorItemSizeStock, int orderPrice, int count) {
        this.colorItemSizeStock = colorItemSizeStock;
        this.orderPrice = orderPrice;
        this.count = count;
        colorItemSizeStock.removeStock(count);
    }

}
