package org.example.tamaapi.dto.responseDto.item;

import lombok.*;
import org.example.tamaapi.domain.Gender;
import org.example.tamaapi.domain.item.Item;

import java.time.LocalDate;


@Getter
@ToString
public class ItemDto {

    private final Long id;

    private final Gender gender;

    private final String yearSeason;

    private final String name;

    private final String description;

    private final LocalDate dateOfManufacture;

    private final String countryOfManufacture;

    private final String manufacturer;

    private final String category;

    private final String textile;

    private final String precaution;

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
