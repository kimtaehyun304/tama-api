package org.example.tamaapi.domain.item;

import jakarta.persistence.*;
import lombok.*;
import org.example.tamaapi.dto.UploadFile;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// 대표 이미지 이외 저장
public class ColorItemImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "color_item_id", nullable = false)
    private ColorItem colorItem;

    @Embedded
    private UploadFile uploadFile;

    @Column(nullable = false)
    private Integer sequence;

    //@Column
    //private String src;

    @Builder
    public ColorItemImage(ColorItem colorItem, UploadFile uploadFile, Integer sequence) {
        this.colorItem = colorItem;
        this.uploadFile = uploadFile;
        this.sequence = sequence;
    }
}
