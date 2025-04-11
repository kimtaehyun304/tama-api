package org.example.tamaapi.domain.item;

import jakarta.persistence.*;
import lombok.*;
import org.example.tamaapi.exception.MyBadRequestException;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ColorItemSizeStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_size_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "color_item_id", nullable = false)
    private ColorItem colorItem;

    @Column(nullable = false)
    private String size;

    private int stock;

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
