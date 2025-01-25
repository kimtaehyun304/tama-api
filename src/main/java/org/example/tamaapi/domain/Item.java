package org.example.tamaapi.domain;

import jakarta.persistence.*;
import lombok.*;
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
    Long id;

    @Column(nullable = false)
    Integer price;

    @Column(nullable = false)
    Integer discountedPrice;

    @Column(nullable = false)
    Gender gender;

    @Column(nullable = false)
    String yearSeason;

    @Column(nullable = false)
    String name;

    @Column(nullable = false)
    String description;

    @Column(nullable = false)
    LocalDate dateOfManufacture;

    @Column(nullable = false)
    String countryOfManufacture;

    @Column(nullable = false)
    String manufacturer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    Category category;

    @Column(nullable = false)
    String textile;

    @Column(nullable = false)
    String precaution;

    @OneToMany(mappedBy = "item")
    @BatchSize(size = 1000)
    List<ColorItem> colorItems = new ArrayList<>();

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
