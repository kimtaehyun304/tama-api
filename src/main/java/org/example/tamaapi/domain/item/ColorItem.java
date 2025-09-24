package org.example.tamaapi.domain.item;

import jakarta.persistence.*;
import lombok.*;
import org.example.tamaapi.domain.BaseEntity;
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
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "color_id", nullable = false)
    private Color color;

    //대표 이미지
    //@Column(nullable = false)
    //private String imageSrc;

    @OneToMany(mappedBy = "colorItem")
    @BatchSize(size = 1000)
    private List<ColorItemImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "colorItem")
    @BatchSize(size = 1000)
    private List<ColorItemSizeStock> colorItemSizeStocks = new ArrayList<>();

    @Builder
    public ColorItem(Item item, Color color) {
        this.item = item;
        this.color = color;
    }

    public void setIdAfterBatch(Long id){
        this.id = id;
    }

}
