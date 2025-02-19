package org.example.tamaapi.domain.item;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tamaapi.domain.BaseEntity;
import org.example.tamaapi.domain.Member;
import org.example.tamaapi.domain.item.ColorItemSizeStock;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_stock_id", nullable = false)
    private ColorItemSizeStock colorItemSizeStock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private int rating;

    private String comment;

    @Builder
    public Review(ColorItemSizeStock colorItemSizeStock, Member member, int rating, String comment) {
        this.colorItemSizeStock = colorItemSizeStock;
        this.member = member;
        this.rating = rating;
        this.comment = comment;
    }

}
