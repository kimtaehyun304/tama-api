package org.example.tamaapi.repository.query;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.example.tamaapi.domain.ColorItem;
import org.example.tamaapi.domain.Gender;
import org.example.tamaapi.domain.Item;
import org.example.tamaapi.dto.requestDto.MySort;
import org.example.tamaapi.dto.responseDto.category.item.RelatedColorItemResponse;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

//DTO 반환이라 extends 엔티티에 맞는게 없음. -> 아무거나 써도 에러 안남. 무난하게 루트 엔티티 적어둠
//DATA JPA 안쓰는 게 어울리나, 순수 JPA는 생산성이 낮아서 안쓰기로 함
//그럴라 했는데 동적 쿼리 있어서 게획 변경
@Repository
@RequiredArgsConstructor
public class ColorItemQueryRepository {

    private final EntityManager em;

    //필터가 없는 쿼리.
    public List<RelatedColorItemQueryDto> findAllByItemIdIn(List<Long> itemsIds) {
        String jpql = "SELECT new org.example.tamaapi.repository.query.RelatedColorItemQueryDto(c, SUM(s.stock)) " +
                "FROM ColorItem c JOIN c.stocks s WHERE c.item.id IN :itemsIds GROUP BY c.id HAVING SUM(s.stock) > 0";

        return em.createQuery(jpql, RelatedColorItemQueryDto.class)
                .setParameter("itemsIds", itemsIds)
                .getResultList();
    }

    /*
    //카테고리 아이템 필터 페이징 IN 절에 쓸 ItemId 리스트
    public List<ColorItem> findAllByCategoryIdInAndFilter(List<Long> categoryIds, Integer minPrice, Integer maxPrice, List<Long> colorIds, List<Gender> genders, Boolean isContainSoldOut) {

        String jpql = "SELECT c FROM ColorItem c JOIN c.item i JOIN c.stocks s WHERE i.category.id IN :categoryIds";

        if (minPrice != null) jpql += " AND i.price >= :minPrice";
        if (maxPrice != null) jpql += " AND i.price <= :maxPrice";
        if (colorIds != null && !colorIds.isEmpty()) jpql += " AND c.color.id IN :colorIds";
        if (genders != null && !genders.isEmpty()) jpql += " AND i.gender IN :genders";
        if (isContainSoldOut == null || Boolean.FALSE.equals(isContainSoldOut)) jpql += " AND s.stock > 0";

        // 그룹화 추가
        jpql += " GROUP BY i.id, c.id";

        TypedQuery<ColorItem> query = em.createQuery(jpql, ColorItem.class);

        query.setParameter("categoryIds", categoryIds);
        if (minPrice != null) query.setParameter("minPrice", minPrice);
        if (maxPrice != null) query.setParameter("maxPrice", maxPrice);
        if (colorIds != null && !colorIds.isEmpty()) query.setParameter("colorIds", colorIds);
        if (genders != null && !genders.isEmpty()) query.setParameter("genders", genders);

        return query.getResultList();
    }
    */

    //카테고리 아이템 필터 페이징 IN 절에 쓸 ItemId 리스트
    //Item 중복 주의
    public List<RelatedColorItemResponse> findAllByCategoryIdInAndFilter(List<Long> categoryIds, Integer minPrice, Integer maxPrice, List<Long> colorIds, List<Gender> genders, Boolean isContainSoldOut, List<MySort> sorts) {

        String jpql = "SELECT new org.example.tamaapi.dto.responseDto.category.item.RelatedColorItemResponse(c, SUM(s.stock)) FROM ColorItem c " +
                "join fetch c.color cl JOIN c.item i JOIN c.stocks s WHERE i.category.id IN :categoryIds order by COALESCE(i.discountedPrice, i.price) desc, i.id desc";

        // WHERE
        if (minPrice != null) jpql += " AND i.price >= :minPrice";
        if (maxPrice != null) jpql += " AND i.price <= :maxPrice";
        if (colorIds != null && !colorIds.isEmpty()) jpql += " AND c.color.id IN :colorIds";
        if (genders != null && !genders.isEmpty()) jpql += " AND i.gender IN :genders";
        if (isContainSoldOut == null || Boolean.FALSE.equals(isContainSoldOut)) jpql += " AND s.stock > 0";

        // ORDER BY

        switch (sorts.get())
        if(sorts.isEmpty()) jpql += " order by COALESCE(i.createdAt, i.id) DESC";
        else {

        }


        if(sorts.get(0).getProperty().equals("price")) jpql += " "
        // 그룹화 추가
        jpql += " GROUP BY i.id, c.id";

        TypedQuery<RelatedColorItemResponse> query = em.createQuery(jpql, RelatedColorItemResponse.class);

        query.setParameter("categoryIds", categoryIds);
        if (minPrice != null) query.setParameter("minPrice", minPrice);
        if (maxPrice != null) query.setParameter("maxPrice", maxPrice);
        if (colorIds != null && !colorIds.isEmpty()) query.setParameter("colorIds", colorIds);
        if (genders != null && !genders.isEmpty()) query.setParameter("genders", genders);

        return query.getResultList();
    }

}
