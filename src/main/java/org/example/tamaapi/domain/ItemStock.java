package org.example.tamaapi.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_size_id")
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "color_item_id", nullable = false)
    ColorItem colorItem;

    @Column(nullable = false)
    String size;

    int stock;

    @Builder
    public ItemStock(ColorItem colorItem, String size, int stock) {
        this.colorItem = colorItem;
        this.size = size;
        this.stock = stock;;
    }

}
