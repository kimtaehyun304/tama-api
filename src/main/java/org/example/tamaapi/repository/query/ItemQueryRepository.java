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

    /*
    @Query("select new org.example.tamaapi.repository.query.ItemMinMaxQueryDto(MIN(COALESCE(i.discountedPrice, i.price)), MAX(COALESCE(i.discountedPrice, i.price))) " +
            "from Item i where i.category.id in :categoryIds")
    Optional<ItemMinMaxQueryDto> findMinMaxPriceByCategoryIdIn(List<Long> categoryIds);
    */

    public Optional<ItemMinMaxQueryDto> findMinMaxPriceByCategoryIdIn(List<Long> categoryIds){
        String jpql = "select new org.example.tamaapi.repository.query.ItemMinMaxQueryDto(" +
                "MIN(COALESCE(i.discountedPrice, i.price)), " +
                "MAX(COALESCE(i.discountedPrice, i.price))) " +
                "from Item i where i.category.id in :categoryIds";
        ItemMinMaxQueryDto itemMinMaxQueryDto = em.createQuery(jpql, ItemMinMaxQueryDto.class).setParameter("categoryIds", categoryIds).getSingleResult();
        return Optional.ofNullable(itemMinMaxQueryDto);
    }

    //페이징용 부모
    public TypedQuery<Item> filter(
            List<Long> categoryIds, Integer minPrice, Integer maxPrice,
            List<Long> colorIds, List<Gender> genders, Boolean isContainSoldOut, int page, int size) {
        /*
        StringBuilder jpql = new StringBuilder(
                "SELECT new org.example.tamaapi.repository.query.ItemQueryDto(i, c, SUM(s.stock)) FROM Item i JOIN i.colorItems c JOIN c.stocks s"
        );
        */

        StringBuilder jpql = new StringBuilder(
                "SELECT i FROM Item i JOIN i.colorItems c JOIN c.stocks s"
        );
        boolean isFirstCondition = true;

        if (categoryIds != null && !categoryIds.isEmpty()) {
            if (isFirstCondition) {
                jpql.append(" WHERE");
                isFirstCondition = false;
            } else {
                jpql.append(" AND");
            }
            jpql.append(" i.category.id IN :categoryIds");
        }

        if (minPrice != null) {
            if (isFirstCondition) {
                jpql.append(" WHERE");
                isFirstCondition = false;
            } else {
                jpql.append(" AND");
            }
            jpql.append(" i.price >= :minPrice");
        }

        if (maxPrice != null) {
            if (isFirstCondition) {
                jpql.append(" WHERE");
                isFirstCondition = false;
            } else {
                jpql.append(" AND");
            }
            jpql.append(" i.price <= :maxPrice");
        }

        if (colorIds != null && !colorIds.isEmpty()) {
            if (isFirstCondition) {
                jpql.append(" WHERE");
                isFirstCondition = false;
            } else {
                jpql.append(" AND");
            }
            jpql.append(" c.color.id IN :colorIds");
        }

        if (genders != null && !genders.isEmpty()) {
            if (isFirstCondition) {
                jpql.append(" WHERE");
                isFirstCondition = false;
            } else {
                jpql.append(" AND");
            }
            jpql.append(" i.gender IN :genders");
        }

        if (isContainSoldOut == null || Boolean.FALSE.equals(isContainSoldOut)) {
            if (isFirstCondition) {
                jpql.append(" WHERE");
                isFirstCondition = false;
            } else {
                jpql.append(" AND");
            }
            jpql.append(" s.stock > 0");
        }

        // 그룹화 추가
        jpql.append(" GROUP BY i.id, c.id");

        TypedQuery<Item> query = em.createQuery(jpql.toString(), Item.class);

        if (categoryIds != null && !categoryIds.isEmpty()) {
            query.setParameter("categoryIds", categoryIds);
        }
        if (minPrice != null) {
            query.setParameter("minPrice", minPrice);
        }
        if (maxPrice != null) {
            query.setParameter("maxPrice", maxPrice);
        }
        if (colorIds != null && !colorIds.isEmpty()) {
            query.setParameter("colorIds", colorIds);
        }
        if (genders != null && !genders.isEmpty()) {
            query.setParameter("genders", genders);
        }
        return query;
    }

    public List<Item> findAllByFilterAndCategoryIdIn(
            List<Long> categoryIds, Integer minPrice, Integer maxPrice,
            List<Long> colorIds, List<Gender> genders, Boolean isContainSoldOut, int page, int size) {

        TypedQuery<Item> query = filter(categoryIds, minPrice, maxPrice, colorIds, genders, isContainSoldOut, page, size);

        // 페이징 처리
        query.setFirstResult((page-1)*size);
        query.setMaxResults(size);

        return query.getResultList();
    }


    public int countAllByFilterAndCategoryIdIn(
            List<Long> categoryIds, Integer minPrice, Integer maxPrice,
            List<Long> colorIds, List<Gender> genders, Boolean isContainSoldOut, int page, int size) {

        TypedQuery<Item> query = filter(categoryIds, minPrice, maxPrice, colorIds, genders, isContainSoldOut, page, size);

        return query.getResultList().size();
    }

}
