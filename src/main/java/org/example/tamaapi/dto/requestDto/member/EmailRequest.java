package org.example.tamaapi.dto.requestDto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.example.tamaapi.domain.Gender;

@Getter
@Setter
public class EmailRequest {

    @NotNull @Email
    private String email;
}
