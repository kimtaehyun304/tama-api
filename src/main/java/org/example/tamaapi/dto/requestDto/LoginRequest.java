package org.example.tamaapi.dto.requestDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginRequest {

    @NotNull @Email
    String email;

    @NotNull
    String password;

}
