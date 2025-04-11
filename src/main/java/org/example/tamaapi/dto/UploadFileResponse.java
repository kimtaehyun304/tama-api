package org.example.tamaapi.dto;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.core.io.UrlResource;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
//파일 경로
public class UploadFileResponse {

    private String originalFileName;
    private UrlResource resource;

}
