package org.example.tamaapi.repository.item.query;


import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;

import org.example.tamaapi.domain.Gender;

import org.example.tamaapi.domain.item.*;
import org.example.tamaapi.dto.UploadFile;
import org.example.tamaapi.dto.requestDto.CustomPageRequest;
import org.example.tamaapi.dto.requestDto.CustomSort;
import org.example.tamaapi.dto.responseDto.CustomPage;

import org.example.tamaapi.dto.responseDto.category.item.RelatedColorItemResponse;
import org.example.tamaapi.exception.MyBadRequestException;
import org.example.tamaapi.repository.item.ColorItemImageRepository;
import org.example.tamaapi.repository.item.query.dto.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.example.tamaapi.domain.QMember.member;
import static org.example.tamaapi.domain.item.QCategory.category;
import static org.example.tamaapi.domain.item.QColor.*;
import static org.example.tamaapi.domain.item.QColorItem.colorItem;
import static org.example.tamaapi.domain.item.QColorItemSizeStock.colorItemSizeStock;
import static org.example.tamaapi.domain.item.QItem.*;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.util.StringUtils.hasText;

//spring data jpa repository는 엔티티 상속 필요
//메서드가 DTO 반환하면 상속한 엔티티랑 안맞아서 애매함. (되긴 함)
//애매하지만 순수 jpa 메서드는 파라미터 할당 필요해서 귀찮기에 spring data jap가 편함
//근데 동적 쿼리 있어서 순수 jpa 씀
@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemQueryRepository {

    private final EntityManager em;
    private final ColorItemImageRepository colorItemImageRepository;
    private final JPAQueryFactory queryFactory;

    //카테고리 아이템 시작
    public CustomPage<CategoryItemQueryDto> findCategoryItems(CustomSort sort, CustomPageRequest customPageRequest, List<Long> categoryIds, String itemName, Integer minPrice, Integer maxPrice, List<Long> colorIds, List<Gender> genders, Boolean isContainSoldOut) {
        //페이징
        List<CategoryItemQueryDto> paging = findCategoryItemsWithPagingAndSort(customPageRequest, sort, categoryIds, itemName, minPrice, maxPrice, colorIds, genders, isContainSoldOut);

        //자식 컬렉션 (해당 페이지 자식만)
        List<Long> pagedItemIds = paging.stream().map(CategoryItemQueryDto::getItemId).toList();
        List<RelatedColorItemResponse> colorItems = findCategoryItemChildren(pagedItemIds, colorIds, isContainSoldOut);

        //커스텀 페이징 변환
        Long rowCount = countCategoryItems(categoryIds,itemName,minPrice,maxPrice,colorIds,genders,isContainSoldOut);
        CustomPage<CategoryItemQueryDto> customPaging = new CustomPage<>(paging, customPageRequest, rowCount);

        //key:itemId
        //페이징에 자식 컬렉션 삽입
        Map<Long, List<RelatedColorItemResponse>> colorItemMap = colorItems.stream().collect(Collectors.groupingBy(RelatedColorItemResponse::getItemId));
        customPaging.getContent().forEach(ci -> ci.setRelatedColorItems(colorItemMap.get(ci.getItemId())));
        return customPaging;
    }

    //페이징 카테고리 아이템
    private List<CategoryItemQueryDto> findCategoryItemsWithPagingAndSort(CustomPageRequest customPageRequest, CustomSort sort, List<Long> categoryIds, String itemName, Integer minPrice, Integer maxPrice, List<Long> colorIds, List<Gender> genders, Boolean isContainSoldOut) {
        return queryFactory
                .select(new QCategoryItemQueryDto(item)).from(item)
                .where(item.id.in(
                        JPAExpressions
                                .select(item.id).distinct().from(item)
                                .join(item.colorItems, colorItem).join(colorItem.colorItemSizeStocks, colorItemSizeStock).join(colorItem.color, color)
                                .where(categoryIdIn(categoryIds), itemNameContains(itemName), minPriceGoe(minPrice), maxPriceLoe(maxPrice), colorIdIn(colorIds), genderIn(genders), isContainSoldOut(isContainSoldOut))))
                .offset(customPageRequest.getPage()-1)
                .limit(customPageRequest.getSize())
                .orderBy(categoryItemSort(sort), new OrderSpecifier<>(Order.DESC, item.id))
                .fetch();
    }

    //페이징 COUNT
    private Long countCategoryItems(List<Long> categoryIds, String itemName, Integer minPrice, Integer maxPrice, List<Long> colorIds, List<Gender> genders, Boolean isContainSoldOut){
        return queryFactory.select(item.id.countDistinct()).from(item)
                .join(item.colorItems, colorItem).join(colorItem.colorItemSizeStocks, colorItemSizeStock).join(colorItem.color, color)
                .where(categoryIdIn(categoryIds), itemNameContains(itemName), minPriceGoe(minPrice), maxPriceLoe(maxPrice), colorIdIn(colorIds), genderIn(genders), isContainSoldOut(isContainSoldOut))
                .fetchOne();
    }

    //페이징 카테고리 아이템 자식 컬렉션
    //페이징 쿼리는 필털링이 안돼있어서 지연 로딩하면 안됨. 자식 컬렉션 필터링 다시 해야함
    private List<RelatedColorItemResponse> findCategoryItemChildren(List<Long> itemIds, List<Long> colorIds, Boolean isContainSoldOut) {
        String jpql = "SELECT new org.example.tamaapi.dto.responseDto.category.item.RelatedColorItemResponse(ci, SUM(s.stock)) FROM ColorItem ci " +
                "join fetch ci.color c JOIN ci.colorItemSizeStocks s WHERE ci.item.id IN :itemIds";

        // 상품명, 가격, 성별은 동적 조건 안필요 (item.id.in에 이미 적용돼있는거)
        if (colorIds != null && !colorIds.isEmpty()) jpql += " AND ci.color.id IN :colorIds";
        //group by한 다음에 해도 결과 같음
        if (isContainSoldOut == null || Boolean.FALSE.equals(isContainSoldOut)) jpql += " AND s.stock > 0";

        // 그룹화 추가 (컬럼 묶는 용)
        jpql += " GROUP BY ci.id";

        TypedQuery<RelatedColorItemResponse> query = em.createQuery(jpql, RelatedColorItemResponse.class);
        query.setParameter("itemIds", itemIds);
        if (colorIds != null && !colorIds.isEmpty()) query.setParameter("colorIds", colorIds);

        List<RelatedColorItemResponse> relatedColorItems = query.getResultList();

        //이미지 세팅
        List<Long> colorItemIds = relatedColorItems.stream().map(RelatedColorItemResponse::getColorItemId).toList();

        //1차캐시 재사용은 findById만 되서 map 씀. 찾는 조건이 pk가 아니라 colorItemId라 findById 안됨
        List<ColorItemImage> colorItemImages = colorItemImageRepository.findAllByColorItemIdInAndSequence(colorItemIds, 1);
        Map<Long, UploadFile> uploadFileMap = colorItemImages.stream().collect(Collectors.toMap(c -> c.getColorItem().getId(), ColorItemImage::getUploadFile));
        
        relatedColorItems.forEach(rci -> rci.setUploadFile(
                uploadFileMap.get(rci.getColorItemId())
        ));

        return relatedColorItems;
    }
    //--카테고리 아이템 끝

    //--카테고리 베스트 아이템 시작
    //DTO 조회는 JOIN FETCH 못 씀.생성자의 파라미터는 SELECT절과 동등, GROUP BY는 SELECE절에 명시된것만 가능
    //orderItem 루트를 colorItem으로 바꿀려고 dto 조회 (근데 어짜피 queryRepository는 재사용할 일 없으니 전부 dto 조회하는 게 난듯)
    //이 정도는 querydsl 안써도 될 듯
    public List<CategoryBestItemQueryDto> findCategoryBestItemWithPaging(List<Long> categoryIds, CustomPageRequest customPageRequest) {
        String jpql = "select new org.example.tamaapi.repository.item.query.dto.CategoryBestItemQueryDto(ci, i) from OrderItem oi " +
                " join oi.colorItemSizeStock ciss join ciss.colorItem ci join ci.item i";

        if (!categoryIds.isEmpty())
            jpql += " where i.category.id in :categoryIds";

        //판매수로 정렬. (사이즈 통합)
        jpql += " group by ci.id order by sum(oi.count) desc";

        TypedQuery<CategoryBestItemQueryDto> query = em.createQuery(jpql, CategoryBestItemQueryDto.class);
        if (!categoryIds.isEmpty()) query.setParameter("categoryIds", categoryIds);

        query.setFirstResult((customPageRequest.getPage() - 1) * customPageRequest.getSize());
        query.setMaxResults(customPageRequest.getSize());
        List<CategoryBestItemQueryDto> categoryBestItemQueryDtos = query.getResultList();

        //이미지 세팅
        List<Long> colorItemIds = categoryBestItemQueryDtos.stream().map(CategoryBestItemQueryDto::getColorItemId).toList();

        //1차캐시 재사용은 findById만 되서 map 씀
        List<ColorItemImage> colorItemImages = colorItemImageRepository.findAllByColorItemIdInAndSequence(colorItemIds, 1);

        Map<Long, UploadFile> uploadFileMap = colorItemImages.stream().collect(Collectors.toMap(c -> c.getColorItem().getId(), ColorItemImage::getUploadFile));
        categoryBestItemQueryDtos.forEach(cbi -> cbi.setUploadFile(
                uploadFileMap.get(cbi.getColorItemId())
        ));

        //리뷰 개수 세팅
        List<CategoryBestItemReviewQueryDto> reviewQueryDtos = findAvgRatingsCountInColorItemId(colorItemIds);
        Map<Long, CategoryBestItemReviewQueryDto> reviewMap = reviewQueryDtos.stream()
                .collect(Collectors.toMap(CategoryBestItemReviewQueryDto::getColorItemId, Function.identity()));

        categoryBestItemQueryDtos.forEach(cbi -> {
                    CategoryBestItemReviewQueryDto reviewQueryDto = reviewMap.get(cbi.getColorItemId());
                    if (reviewQueryDto != null) {
                        cbi.setAvgRating(reviewQueryDto.getAvgRating());
                        cbi.setReviewCount(reviewQueryDto.getReviewCount());
                    }
                }
        );

        return categoryBestItemQueryDtos;
    }

    //IDE 에러 없애랴고 cast 해줌 (안해도 작동은 함)
    private List<CategoryBestItemReviewQueryDto> findAvgRatingsCountInColorItemId(List<Long> colorItemIds) {
        String jpql = "select new org.example.tamaapi.repository.item.query.dto.CategoryBestItemReviewQueryDto(ci.id, CAST(ROUND(AVG(r.rating), 1) AS double), count(ci.id)) from Review r" +
                " join r.orderItem oi join oi.colorItemSizeStock isk join isk.colorItem ci where ci.id in :colorItemIds" +
                " group by ci.id";
        TypedQuery<CategoryBestItemReviewQueryDto> query = em.createQuery(jpql, CategoryBestItemReviewQueryDto.class);
        query.setParameter("colorItemIds", colorItemIds);
        return query.getResultList();
    }
    //--카테고리 베스트 아이템 끝

    private OrderSpecifier<?> categoryItemSort(CustomSort sort){
        Order direction = sort.getDirection().isAscending() ? Order.ASC : Order.DESC;
        return switch (sort.getProperty()) {
            case "price" -> new OrderSpecifier< >(direction, Expressions.numberTemplate(Integer.class, "coalesce({0}, {1})", item.discountedPrice, item.price));
            case "createdAt" -> new OrderSpecifier<>(Order.DESC, item.createdAt);
            default -> throw new MyBadRequestException("유효한 property가 없습니다.");
        };
    }

    private BooleanExpression itemNameContains(String itemName) {
        return hasText(itemName) ? item.name.contains(itemName) : null;
    }

    private BooleanExpression categoryIdIn(List<Long> categoryIds) {
        return isEmpty(categoryIds) ? null : category.id.in(categoryIds);
    }

    private BooleanExpression minPriceGoe(Integer minPrice) {
        return minPrice == null ? null : item.price.goe(minPrice);
    }

    private BooleanExpression maxPriceLoe(Integer maxPrice) {
        return maxPrice == null ? null : item.price.loe(maxPrice);
    }

    private BooleanExpression colorIdIn(List<Long> colorIds) {
        return isEmpty(colorIds) ? null : color.id.in(colorIds);
    }

    private BooleanExpression genderIn(List<Gender> genders) {
        return isEmpty(genders) ? null : item.gender.in(genders);
    }

    private BooleanExpression isContainSoldOut(Boolean isContainSoldOut) {
        return isTrue(isContainSoldOut) ? null : colorItemSizeStock.stock.gt(0);
    }



    /*
    //가격 최소값, 최대값. 페이징 쿼리랑 별도로 요청되서 in 절 못 씀. 필터 필요
    //group by 불필요
    public Optional<ItemMinMaxQueryDto> findMinMaxPriceByCategoryIdInAndFilter(List<Long> categoryIds, Integer minPrice, Integer maxPrice, List<Long> colorIds, List<Gender> genders, Boolean isContainSoldOut) {

        String jpql = "SELECT new org.example.tamaapi.repository.item.query.dto.ItemMinMaxQueryDto(MIN(COALESCE(i.discountedPrice, i.price)), MAX(COALESCE(i.discountedPrice, i.price))) FROM ColorItem ci " +
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

     */

}
