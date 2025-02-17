package org.example.tamaapi.repository.query;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.example.tamaapi.domain.Gender;
import org.example.tamaapi.domain.Item;
import org.example.tamaapi.dto.requestDto.MyPageRequest;
import org.example.tamaapi.dto.requestDto.MySort;
import org.example.tamaapi.dto.responseDto.category.item.CategoryItemResponse;
import org.example.tamaapi.dto.responseDto.category.item.RelatedColorItemResponse;
import org.example.tamaapi.exception.MyBadRequestException;
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

    //--카테고리 아이템 로직 시작
    //페이징 where in 절에 쓸 itemIds, rowCount & 페이징 자식 컬렉션에 쓸 colorItemIds(지연 로딩)
    public List<Item> findItemsByCategoryIdInAndFilter(List<Long> categoryIds, Integer minPrice, Integer maxPrice, List<Long> colorIds, List<Gender> genders, Boolean isContainSoldOut) {
        String jpql = "SELECT i FROM Item i JOIN i.colorItems ci JOIN ci.colorItemSizeStocks s JOIN ci.color WHERE i.category.id IN :categoryIds";

        // WHERE
        if (minPrice != null) jpql += " AND COALESCE(i.discountedPrice, i.price) >= :minPrice";
        if (maxPrice != null) jpql += " AND COALESCE(i.discountedPrice, i.price) <= :maxPrice";
        if (colorIds != null && !colorIds.isEmpty()) jpql += " AND ci.color.id IN :colorIds";
        if (genders != null && !genders.isEmpty()) jpql += " AND i.gender IN :genders";
        if (isContainSoldOut == null || Boolean.FALSE.equals(isContainSoldOut)) jpql += " AND s.stock > 0";

        // 그룹화 추가
        jpql += " GROUP BY i.id, ci.id";

        TypedQuery<Item> query = em.createQuery(jpql, Item.class);

        query.setParameter("categoryIds", categoryIds);
        if (minPrice != null) query.setParameter("minPrice", minPrice);
        if (maxPrice != null) query.setParameter("maxPrice", maxPrice);
        if (colorIds != null && !colorIds.isEmpty()) query.setParameter("colorIds", colorIds);
        if (genders != null && !genders.isEmpty()) query.setParameter("genders", genders);

        return query.getResultList();
    }

    //페이징, 정렬
    public List<CategoryItemResponse> findAllByItemIdIn(List<Long> itemIds, MySort sort, MyPageRequest myPageRequest) {
        String jpql = "SELECT new org.example.tamaapi.dto.responseDto.category.item.CategoryItemResponse(i) FROM Item i WHERE i.id in :itemIds";

        // ORDER BY. 컨트롤러 sort required true -> sort null X
        switch (sort.getProperty()) {
            case "price" ->
                    jpql += String.format(" order by COALESCE(i.discountedPrice, i.price) %s, i.id DESC", sort.getDirection());
            case "createdAt" -> jpql += " order by i.createdAt DESC, i.id DESC";
            default -> throw new MyBadRequestException("유효한 property가 없습니다.");
        }

        TypedQuery<CategoryItemResponse> query = em.createQuery(jpql, CategoryItemResponse.class);
        query.setParameter("itemIds", itemIds);

        query.setFirstResult((myPageRequest.getPage() - 1) * myPageRequest.getSize());
        query.setMaxResults(myPageRequest.getSize());
        return query.getResultList();
    }

    //페이징 아이템 자식 컬렉션. 컨트롤러에서 루프 돌며 페이징 객체에 삽입
    //페이징 쿼리는 colorItems 필터링이 날라가서, 다시 필터링해야함
    public List<RelatedColorItemResponse> findColorItemsByCategoryIdInAndFilter(List<Long> itemIds, List<Long> colorIds, Boolean isContainSoldOut) {

        String jpql = "SELECT new org.example.tamaapi.dto.responseDto.category.item.RelatedColorItemResponse(ci, SUM(s.stock)) FROM ColorItem ci " +
                "join fetch ci.color c JOIN ci.item i JOIN ci.colorItemSizeStocks s WHERE i.id IN :itemIds";

        // WHERE. 가격,성별은 IN itemIds
        if (colorIds != null && !colorIds.isEmpty()) jpql += " AND ci.color.id IN :colorIds";
        if (isContainSoldOut == null || Boolean.FALSE.equals(isContainSoldOut)) jpql += " AND s.stock > 0";

        // 그룹화 추가
        jpql += " GROUP BY i.id, ci.id";

        TypedQuery<RelatedColorItemResponse> query = em.createQuery(jpql, RelatedColorItemResponse.class);
        query.setParameter("itemIds", itemIds);
        if (colorIds != null && !colorIds.isEmpty()) query.setParameter("colorIds", colorIds);

        return query.getResultList();
    }

    //가격 최소값, 최대값. 페이징 쿼리랑 별도로 요청되서 in 절 못 씀. 필터 필요
    //group by 불필요
    public Optional<ItemMinMaxQueryDto> findMinMaxPriceByCategoryIdInAndFilter(List<Long> categoryIds, Integer minPrice, Integer maxPrice, List<Long> colorIds, List<Gender> genders, Boolean isContainSoldOut) {

        String jpql = "SELECT new org.example.tamaapi.repository.query.ItemMinMaxQueryDto(MIN(COALESCE(i.discountedPrice, i.price)), MAX(COALESCE(i.discountedPrice, i.price))) FROM ColorItem ci " +
                "JOIN ci.item i JOIN ci.colorItemSizeStocks s WHERE i.category.id IN :categoryIds";

        // WHERE
        if (minPrice != null) jpql += " AND COALESCE(i.discountedPrice, i.price) >= :minPrice";
        if (maxPrice != null) jpql += " AND COALESCE(i.discountedPrice, i.price) <= :maxPrice";
        if (colorIds != null && !colorIds.isEmpty()) jpql += " AND ci.color.id IN :colorIds";
        if (genders != null && !genders.isEmpty()) jpql += " AND i.gender IN :genders";
        if (isContainSoldOut == null || Boolean.FALSE.equals(isContainSoldOut)) jpql += " AND s.stock > 0";

        TypedQuery<ItemMinMaxQueryDto> query = em.createQuery(jpql, ItemMinMaxQueryDto.class);

        query.setParameter("categoryIds", categoryIds);
        if (minPrice != null) query.setParameter("minPrice", minPrice);
        if (maxPrice != null) query.setParameter("maxPrice", maxPrice);
        if (colorIds != null && !colorIds.isEmpty()) query.setParameter("colorIds", colorIds);
        if (genders != null && !genders.isEmpty()) query.setParameter("genders", genders);

        return Optional.ofNullable(query.getSingleResult());

    }
    //--카테고리 아이템 로직 끝
}
