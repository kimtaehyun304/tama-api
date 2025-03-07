package org.example.tamaapi.dto.responseDto.item;

import lombok.*;
import org.example.tamaapi.domain.Gender;
import org.example.tamaapi.domain.item.Item;

import java.time.LocalDate;


@Getter
@ToString
public class ItemDto {

    private Long id;

    private Gender gender;

    private String yearSeason;

    private String name;

    private String description;

    private LocalDate dateOfManufacture;

    private String countryOfManufacture;

    private String manufacturer;

    private String category;

    private String textile;

    private String precaution;

    public ItemDto(Item item) {
        id = item.getId();
        gender= item.getGender();
        yearSeason = item.getYearSeason();
        name = item.getName();
        description = item.getDescription();
        dateOfManufacture = item.getDateOfManufacture();
        countryOfManufacture = item.getCountryOfManufacture();
        manufacturer = item.getManufacturer();
        category = item.getCategory().getName();
        textile = item.getTextile();
        precaution = item.getPrecaution();
    }
}
