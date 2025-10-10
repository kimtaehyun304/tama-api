package org.example.tamaapi.domain.item;

import jakarta.persistence.*;
import lombok.*;
import org.example.tamaapi.domain.order.OrderItem;
import org.example.tamaapi.exception.MyBadRequestException;
import org.hibernate.annotations.DynamicUpdate;

import java.util.ArrayList;
import java.util.List;

@Table(indexes = {
        //추가해도 속도 동일
        //@Index(name = "idx_colorItemId_stock", columnList = "color_item_id, stock"),
        //@Index(name = "idx_stock", columnList = "stock"),
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

    @Column(nullable = false)
    private int stock;

    @OneToMany(mappedBy = "colorItemSizeStock")
    private List<OrderItem> orderItems = new ArrayList<>();

    @Builder
    public ColorItemSizeStock(ColorItem colorItem, String size, int stock) {
        this.colorItem = colorItem;
        this.size = size;
        this.stock = stock;
    }

    /* 동시 요청시 재고 음수되는 가능성
    public void removeStock(int quantity) {
        int restStock = this.stock - quantity;
        if (restStock < 0)
            throw new MyBadRequestException("재고가 부족합니다.");
        this.stock = restStock;
    }
     */

    public void removeStock(int quantity) {
        int restStock = this.stock - quantity;
        if (restStock < 0)
            throw new MyBadRequestException("재고가 부족합니다.");
        this.stock = restStock;
    }


    public void changeStock(int quantity) {
        this.stock = quantity;
    }

}
