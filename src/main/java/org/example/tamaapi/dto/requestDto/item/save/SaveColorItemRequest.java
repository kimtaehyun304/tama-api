package org.example.tamaapi.dto.requestDto.item.save;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SaveColorItemRequest {

    private Long colorId;

    //이미지
    private List<MultipartFile> files;
    
    private List<SaveSizeStockRequest> sizeStocks;

}
