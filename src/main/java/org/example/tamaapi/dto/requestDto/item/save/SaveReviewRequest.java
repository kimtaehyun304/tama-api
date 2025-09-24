package org.example.tamaapi.dto.requestDto.item.save;

import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
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
public class SaveReviewRequest {

    @NotNull @Positive
    private Long orderItemId;

    @NotNull @Positive @Max(5)
    private Integer rating;

    @NotBlank
    private String comment;

    private Integer height;

    private Integer weight;

}
