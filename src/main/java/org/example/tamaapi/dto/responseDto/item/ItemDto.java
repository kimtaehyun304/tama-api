package org.example.tamaapi.dto.responseDto.item;

import lombok.*;
import org.example.tamaapi.domain.Gender;
import org.example.tamaapi.domain.Item;

import java.time.LocalDate;


@Getter
@ToString
public class ItemDto {

    Long id;

    Gender gender;

    String yearSeason;

    String name;

    String description;

    LocalDate dateOfManufacture;

    String countryOfManufacture;

    String manufacturer;

    String category;

    String textile;

    String precaution;

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
