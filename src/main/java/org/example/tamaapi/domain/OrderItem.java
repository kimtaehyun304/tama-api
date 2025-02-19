package org.example.tamaapi.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.tamaapi.domain.item.ColorItemSizeStock;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "color_item_size_stock_id")
    private ColorItemSizeStock colorItemSizeStock;

    //할인 쿠폰 적용될 수 있음. 복잡해서 미구현
    //private int totalPrice;

    private int count;

    //setOrder는 createOrder에서 연관메서드로 함
    @Builder
    public OrderItem(ColorItemSizeStock colorItemSizeStock, int count) {
        this.colorItemSizeStock = colorItemSizeStock;
        this.count = count;
        colorItemSizeStock.removeStock(count);
    }

}
