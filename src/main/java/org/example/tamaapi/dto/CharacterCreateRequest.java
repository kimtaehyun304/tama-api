package org.example.tamaapi.dto;

import lombok.*;

@Getter
@ToString
public class CharacterCreateRequest {
    private String name;
    private Long age;
}