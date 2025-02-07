package org.example.tamaapi.repository.query;

import ch.qos.logback.core.util.StringUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.example.tamaapi.domain.ColorItem;
import org.example.tamaapi.domain.Gender;
import org.example.tamaapi.domain.Item;
import org.example.tamaapi.dto.requestDto.MyPageRequest;
import org.example.tamaapi.dto.requestDto.MySort;
import org.example.tamaapi.dto.responseDto.category.item.CategoryItemResponse;
import org.example.tamaapi.dto.responseDto.category.item.RelatedColorItemResponse;
import org.example.tamaapi.exception.MyBadRequestException;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

//DTO 반환이라 extends 엔티티에 맞는게 없음. -> 아무거나 써도 에러 안남. 무난하게 루트 엔티티 적어둠
//DATA JPA 안쓰는 게 어울리나, 순수 JPA는 생산성이 낮아서 안쓰기로 함
//그럴라 했는데 동적 쿼리 있어서 게획 변경
@Repository
@RequiredArgsConstructor
public class ColorItemQueryRepository {

    private final EntityManager em;



}
