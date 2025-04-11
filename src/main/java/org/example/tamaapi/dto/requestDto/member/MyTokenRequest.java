package org.example.tamaapi.dto.requestDto.member;


import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class MyTokenRequest {
    @NotNull
    private String tempToken;
}
