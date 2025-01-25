package org.example.tamaapi.dto.responseDto.item;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tamaapi.domain.ColorItem;


@Getter
// 대표 이미지 이외 저장
public class ItemImageDto {

    String src;

}
