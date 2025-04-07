package org.example.tamaapi.domain.item;

import jakarta.persistence.*;
import lombok.*;
import org.example.tamaapi.domain.BaseEntity;
import org.example.tamaapi.domain.Gender;

import org.hibernate.annotations.BatchSize;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private Integer discountedPrice;

    @Column(nullable = false)
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
    @BatchSize(size = 1000)
    private final List<ColorItem> colorItems = new ArrayList<>();

    @Builder
    public Item(Integer price, Integer discountedPrice, Gender gender, String yearSeason, String name, String description, LocalDate dateOfManufacture, String countryOfManufacture, String manufacturer, Category category, String textile, String precaution) {
        this.price = price;
        this.discountedPrice = discountedPrice;
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

}
