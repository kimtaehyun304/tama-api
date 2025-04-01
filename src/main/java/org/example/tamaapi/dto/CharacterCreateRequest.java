package org.example.tamaapi.dto;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CharacterCreateRequest {
    private String name;
    private Long age;
}