package org.example.tamaapi.dto.requestDto.item.save;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tamaapi.domain.Gender;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SaveItemRequest {

    //상품 정보
    @NotBlank
    private String name;

    @NotNull
    private Long categoryId;

    @NotNull
    private Integer originalPrice;

    @NotNull
    private Integer nowPrice;

    @NotNull
    private Gender gender;

    //상품 상세 정보
    @NotBlank
    private String description;

    @NotBlank
    private String yearSeason;

    @NotNull
    private LocalDate dateOfManufacture;

    @NotBlank
    private String countryOfManufacture;

    @NotBlank
    private String manufacturer;

    @NotBlank
    private String textile;

    @NotBlank
    private String precaution;

    @Size(min = 1)
    private List<SaveColorItemRequest> colorItems;

}
