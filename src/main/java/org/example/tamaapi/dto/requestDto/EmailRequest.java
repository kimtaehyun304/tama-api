package org.example.tamaapi.dto.requestDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.example.tamaapi.domain.Gender;

@Getter
@Setter
public class EmailRequest {

    @NotNull @Email
    String email;
}
