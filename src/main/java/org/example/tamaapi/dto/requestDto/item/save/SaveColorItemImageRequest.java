package org.example.tamaapi.dto.requestDto.item.save;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class SaveColorItemImageRequest {
    @NotNull
    private Long colorItemId;

    @NotEmpty
    private List<MultipartFile> files;
}

