package org.example.tamaapi.dto.requestDto.item.save;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SaveColorItemImageWrapperRequest {

    @Valid
    @NotEmpty
    private List<SaveColorItemImageRequest> requests;
}
