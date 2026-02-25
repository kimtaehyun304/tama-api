package org.example.tamaapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.example.tamaapi.domain.Gender;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true) // GPT가 이상한 필드 줘도 무시
@JsonInclude(JsonInclude.Include.NON_EMPTY) // null + "" + [] 전부 제거
public class RecommendedSqlCondition {

    @JsonProperty("성별")
    private List<Gender> genders;

    @JsonProperty("시즌")
    private String seasonKeyword;

    @JsonProperty("카테고리")
    private List<String> categoryNames;

    @JsonProperty("최소가격")
    private Integer minPrice;

    @JsonProperty("최대가격")
    private Integer maxPrice;

    @JsonProperty("색상")
    private List<String> colorNames;

    @JsonProperty("소재")
    private String textile;

    //false 일때만 보내는게 의미있지만 로직 번거로워서 jsonIgnore
    @JsonIgnore
    private Boolean isContainSoldOut;

    @JsonProperty("설명 키워드")
    private List<String> descriptionKeywords;
}
