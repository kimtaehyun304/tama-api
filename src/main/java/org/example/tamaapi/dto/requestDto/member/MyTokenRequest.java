package org.example.tamaapi.dto.requestDto.member;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class MyTokenRequest {
    @NotBlank
    private String tempToken;
}
