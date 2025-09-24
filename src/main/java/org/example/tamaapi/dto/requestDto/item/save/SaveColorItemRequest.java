package org.example.tamaapi.dto.requestDto.item.save;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tamaapi.domain.item.ColorItem;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SaveColorItemRequest {

    private Long colorId;

    private List<SaveSizeStockRequest> sizeStocks;


}
