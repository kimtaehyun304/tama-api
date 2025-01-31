package org.example.tamaapi.repository.query;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

//DTO 반환이라 extends 엔티티에 맞는게 없음. -> 아무거나 써도 에러 안남. 무난하게 루트 엔티티 적어둠
//DATA JPA 안쓰는 게 어울리나, 순수 JPA는 생산성이 낮아서 안쓰기로 함
//그럴라 했는데 동적 쿼리 있어서 게획 변경
@Repository
@RequiredArgsConstructor
public class ColorItemQueryRepository2 {

    private final EntityManager em;

    //필터가 없는 쿼리.
    public List<RelatedColorItemQueryDto> findAllByItemIdIn(List<Long> itemsIds) {
        String jpql = "SELECT new org.example.tamaapi.repository.query.RelatedColorItemQueryDto(c, SUM(s.stock)) " +
                "FROM ColorItem c JOIN c.stocks s WHERE c.item.id IN :itemsIds GROUP BY c.id HAVING SUM(s.stock) > 0";

        return em.createQuery(jpql, RelatedColorItemQueryDto.class)
                .setParameter("itemsIds", itemsIds)
                .getResultList();
    }



}
