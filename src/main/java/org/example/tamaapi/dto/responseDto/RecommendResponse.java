package org.example.tamaapi.dto.responseDto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.tamaapi.domain.Gender;
import org.example.tamaapi.dto.RecommendedSqlCondition;
import org.example.tamaapi.repository.item.query.dto.RecommendedItemQueryResponse;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class RecommendResponse {

    private RecommendedSqlCondition recommendedCondition;
    private List<RecommendedItemQueryResponse> recommendedItems;

}
