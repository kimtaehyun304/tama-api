package org.example.tamaapi.dto;

import jakarta.persistence.Embeddable;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Embeddable
public class UploadFile {

    private String originalFileName;
    private String storedFileName;



}
