package org.example.tamaapi.domain.item;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tamaapi.domain.item.ColorItem;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// 대표 이미지 이외 저장
public class ItemImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "color_item_id")
    private ColorItem colorItem;

    @Column
    private String src;

    @Builder
    public ItemImage(Long id, ColorItem colorItem, String src) {
        this.id = id;
        this.colorItem = colorItem;
        this.src = src;
    }
}
