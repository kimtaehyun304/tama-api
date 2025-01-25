package org.example.tamaapi.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// 대표 이미지 이외 저장
public class ItemImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_image_id")
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "color_item_id")
    ColorItem colorItem;

    @Column
    String src;

    @Builder
    public ItemImage(Long id, ColorItem colorItem, String src) {
        this.id = id;
        this.colorItem = colorItem;
        this.src = src;
    }
}
