package org.example.tamaapi.dto.requestDto;


import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class MyTokenRequest {
    @NotNull
    String tempToken;
}
