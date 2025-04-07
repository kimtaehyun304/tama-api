package org.example.tamaapi.dto.responseDto.item;

import lombok.Getter;
import lombok.ToString;
import org.example.tamaapi.domain.Gender;
import org.example.tamaapi.domain.item.Item;

import java.time.LocalDate;
import java.util.List;


@Getter
public class SavedColorItemIdResponse {

    private final List<Long> savedColorItemIds;

    public SavedColorItemIdResponse(List<Long> savedColorItemIds) {
        this.savedColorItemIds = savedColorItemIds;
    }
}
