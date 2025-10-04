package org.example.tamaapi.domain.item;

import jakarta.persistence.*;
import lombok.*;
import org.example.tamaapi.domain.BaseEntity;
import org.example.tamaapi.domain.Gender;

import org.hibernate.annotations.BatchSize;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
//그룹바이에 복합 인덱스 적용하려면 PK도 같이 있어야함
@Table(indexes = {
        //@Index(name = "idx_nowPrice", columnList = "nowPrice"),
        //@Index(name = "idx_gender", columnList = "gender"),
        //@Index(name = "idx_name_original_now_price", columnList = "name, originalPrice, nowPrice")
        //@Index(name = "idx_gender_nowPrice", columnList = "gender, nowPrice"),
        //@Index(name = "idx_category_gender", columnList = "category_id, gender"),
        //@Index(name = "idx_category_nowPrice", columnList = "category_id, nowPrice"),
        //@Index(name = "idx_category_gender_nowPrice", columnList = "category_id, gender, nowPrice")
        //@Index(name = "idx_item_id,name_originalPrice_nowPrice_gender", columnList = "category_id, gender, nowPrice")
        //@Index(name = "idx_gender_nowPrice_id_name_originalPrice", columnList = "gender, nowPrice, item_id, name, originalPrice")
        //@Index(name = "idx_gender_nowPrice_name_id_originalPrice", columnList = "gender, nowPrice, name, item_id, originalPrice")
        //temporary,filesort 개선용 (없는게 더 남)
        //@Index(name = "idx_id_gender_nowPrice_name_originalPrice", columnList = "item_id, gender, nowPrice, name, originalPrice")
        //@Index(name = "idx_gender_nowPrice_id", columnList = "gender, nowPrice, item_id")
})
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class Item extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @Column(nullable = false)
    private Integer originalPrice;

    @Column(nullable = false)
    private Integer nowPrice;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = false)
    private String yearSeason;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDate dateOfManufacture;

    @Column(nullable = false)
    private String countryOfManufacture;

    @Column(nullable = false)
    private String manufacturer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    private String textile;

    @Column(nullable = false)
    private String precaution;

    @OneToMany(mappedBy = "item")
    private List<ColorItem> colorItems = new ArrayList<>();

    @Builder
    public Item(Integer originalPrice, Integer nowPrice, Gender gender, String yearSeason, String name, String description, LocalDate dateOfManufacture, String countryOfManufacture, String manufacturer, Category category, String textile, String precaution) {
        this.originalPrice = originalPrice;
        this.nowPrice = nowPrice;
        this.gender = gender;
        this.yearSeason = yearSeason;
        this.name = name;
        this.description = description;
        this.dateOfManufacture = dateOfManufacture;
        this.countryOfManufacture = countryOfManufacture;
        this.manufacturer = manufacturer;
        this.category = category;
        this.textile = textile;
        this.precaution = precaution;
    }

    //배치작업 용
    public void setIdByReturningId(Long id) {
        this.id = id;
    }
}
