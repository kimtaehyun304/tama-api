package org.example.tamaapi.domain.item;

import jakarta.persistence.*;
import lombok.*;
import org.example.tamaapi.domain.order.OrderItem;
import org.example.tamaapi.exception.MyBadRequestException;
import org.hibernate.annotations.DynamicUpdate;

import java.util.ArrayList;
import java.util.List;

@Table(indexes = {
        //@Index(name = "idx_colorItemId_stock", columnList = "color_item_id, stock"),
        //이게 더 explain filterd 지수 높음
        @Index(name = "idx_stock", columnList = "stock"),
})
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
public class ColorItemSizeStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "color_item_size_stock_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "color_item_id", nullable = false)
    private ColorItem colorItem;

    @Column(nullable = false)
    private String size;

    private int stock;

    @OneToMany(mappedBy = "colorItemSizeStock")
    private List<OrderItem> orderItems = new ArrayList<>();

    @Builder
    public ColorItemSizeStock(ColorItem colorItem, String size, int stock) {
        this.colorItem = colorItem;
        this.size = size;
        this.stock = stock;
    }

    public void removeStock(int quantity) {
        int restStock = this.stock - quantity;
        if (restStock < 0) {
            throw new MyBadRequestException("재고가 부족합니다.");
        }
        this.stock = restStock;
    }

}
