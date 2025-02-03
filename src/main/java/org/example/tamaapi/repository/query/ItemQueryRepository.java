package org.example.tamaapi.repository.query;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.example.tamaapi.domain.Gender;
import org.example.tamaapi.domain.Item;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

//DTO 반환이라 extends 엔티티에 맞는게 없음. -> 아무거나 써도 에러 안남. 무난하게 루트 엔티티 적어둠
//DATA JPA 안쓰는 게 어울리나, 순수 JPA는 생산성이 낮아서 안쓰기로 함
//그럴라 했는데 동적 쿼리 있어서 게획 변경
@Repository
@RequiredArgsConstructor
public class ItemQueryRepository {

    private final EntityManager em;


    public Optional<ItemMinMaxQueryDto> findMinMaxPriceByCategoryIdIn(List<Long> categoryIds) {
        String jpql = "select new org.example.tamaapi.repository.query.ItemMinMaxQueryDto(" +
                "MIN(COALESCE(i.discountedPrice, i.price)), " +
                "MAX(COALESCE(i.discountedPrice, i.price))) " +
                "from Item i where i.category.id in :categoryIds";
        ItemMinMaxQueryDto itemMinMaxQueryDto = em.createQuery(jpql, ItemMinMaxQueryDto.class).setParameter("categoryIds", categoryIds).getSingleResult();
        return Optional.ofNullable(itemMinMaxQueryDto);
    }






}
