package org.example.tamaapi.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ColorItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "color_item_id")
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "color_id", nullable = false)
    Color color;

    //대표 이미지
    @Column(nullable = false)
    String imageSrc;

    @OneToMany(mappedBy = "colorItem")
    @BatchSize(size = 1000)
    List<ItemStock> stocks = new ArrayList<>();

    @Builder
    public ColorItem(Item item, Color color, String imageSrc) {
        this.item = item;
        this.color = color;
        this.imageSrc = imageSrc;
    }
}
