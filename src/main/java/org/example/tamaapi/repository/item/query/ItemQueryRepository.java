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
import org.example.tamaapi.domain.order.QOrderItem;
import org.example.tamaapi.dto.UploadFile;
import org.example.tamaapi.dto.requestDto.CustomPageRequest;
import org.example.tamaapi.dto.requestDto.CustomSort;
import org.example.tamaapi.dto.responseDto.CustomPage;

import org.example.tamaapi.repository.item.query.dto.RelatedColorItemResponse;
import org.example.tamaapi.exception.MyBadRequestException;
import org.example.tamaapi.repository.item.ColorItemImageRepository;
import org.example.tamaapi.repository.item.query.dto.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.example.tamaapi.domain.item.QCategory.category;
import static org.example.tamaapi.domain.item.QColor.*;
import static org.example.tamaapi.domain.item.QColorItem.colorItem;
import static org.example.tamaapi.domain.item.QColorItemSizeStock.colorItemSizeStock;
import static org.example.tamaapi.domain.item.QItem.*;
import static org.example.tamaapi.domain.order.QOrderItem.orderItem;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.util.StringUtils.hasText;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemQueryRepository {

    private final EntityManager em;
    private final ColorItemImageRepository colorItemImageRepository;
    private final JPAQueryFactory queryFactory;

    //카테고리 아이템
    public CustomPage<CategoryItemQueryDto> findCategoryItemsWithPagingAndSort(CustomSort sort, CustomPageRequest customPageRequest, List<Long> categoryIds, String itemName, Integer minPrice, Integer maxPrice, List<Long> colorIds, List<Gender> genders, Boolean isContainSoldOut) {
        //페이징
        List<CategoryItemQueryDto> paging = findCategoryItemsParentWithPagingAndSort(customPageRequest, sort, categoryIds, itemName, minPrice, maxPrice, colorIds, genders, isContainSoldOut);

        //자식 컬렉션 (해당 페이지 자식만)
        List<Long> pagedItemIds = paging.stream().map(CategoryItemQueryDto::getItemId).toList();
        List<RelatedColorItemResponse> colorItems = findCategoryItemsChildren(pagedItemIds, colorIds, isContainSoldOut);

        //커스텀 페이징 변환
        Long rowCount = countCategoryItems(categoryIds, itemName, minPrice, maxPrice, colorIds, genders, isContainSoldOut);
        CustomPage<CategoryItemQueryDto> customPaging = new CustomPage<>(paging, customPageRequest, rowCount);

        //key:itemId
        //페이징에 자식 컬렉션 삽입
        Map<Long, List<RelatedColorItemResponse>> colorItemMap = colorItems.stream().collect(Collectors.groupingBy(RelatedColorItemResponse::getItemId));
        customPaging.getContent().forEach(ci -> ci.setRelatedColorItems(colorItemMap.get(ci.getItemId())));
        return customPaging;
    }

    //페이징 카테고리 아이템 부모
    private List<CategoryItemQueryDto> findCategoryItemsParentWithPagingAndSort(CustomPageRequest customPageRequest, CustomSort sort, List<Long> categoryIds, String itemName, Integer minPrice, Integer maxPrice, List<Long> colorIds, List<Gender> genders, Boolean isContainSoldOut) {
        return queryFactory
                .select(new QCategoryItemQueryDto(item.id, item.name, item.price, item.discountedPrice)).from(item)
                .where(item.id.in(
                        JPAExpressions
                                .select(item.id).distinct().from(item)
                                .join(item.colorItems, colorItem).join(colorItem.colorItemSizeStocks, colorItemSizeStock).join(colorItem.color, color)
                                .where(categoryIdIn(categoryIds), itemNameContains(itemName), minPriceGoe(minPrice), maxPriceLoe(maxPrice), colorIdIn(colorIds), genderIn(genders), isContainSoldOut(isContainSoldOut))))
                .offset(customPageRequest.getPage() - 1)
                .limit(customPageRequest.getSize())
                .orderBy(categoryItemSort(sort), new OrderSpecifier<>(Order.DESC, item.id))
                .fetch();
    }

    //페이징 COUNT (최적화를 위해 따로 분리)
    private Long countCategoryItems(List<Long> categoryIds, String itemName, Integer minPrice, Integer maxPrice, List<Long> colorIds, List<Gender> genders, Boolean isContainSoldOut) {
        return queryFactory.select(item.id.countDistinct()).from(item)
                .join(item.colorItems, colorItem).join(colorItem.colorItemSizeStocks, colorItemSizeStock).join(colorItem.color, color)
                .where(categoryIdIn(categoryIds), itemNameContains(itemName), minPriceGoe(minPrice), maxPriceLoe(maxPrice), colorIdIn(colorIds), genderIn(genders), isContainSoldOut(isContainSoldOut))
                .fetchOne();
    }

    //페이징 카테고리 아이템 자식 컬렉션 (@batchSize는 모든 자식 컬렉션을 가져오는 거라 부적절)
    //InItemIds로 item 컬럼에 해당하는 검색 조건 대체 가능
    private List<RelatedColorItemResponse> findCategoryItemsChildren(List<Long> itemIds, List<Long> colorIds, Boolean isContainSoldOut) {

        List<RelatedColorItemResponse> relatedColorItems = queryFactory.select
                        (new QRelatedColorItemResponse(colorItem.item.id, colorItem.id, color.name, color.hexCode, colorItemSizeStock.stock.sum()))
                .from(colorItem).join(colorItem.color, color).join(colorItem.colorItemSizeStocks, colorItemSizeStock)
                .where(itemIdIn(itemIds), colorIdIn(colorIds), isContainSoldOut(isContainSoldOut))
                .groupBy(colorItem.id)
                .fetch();

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

    //--------------------------------------------------------------------------------------------------------------------------------------------------------
    //카테고리 베스트 아이템

    //orderItem 루트를 colorItem으로 바꿀려고 dto 조회 (근데 어짜피 queryRepository는 재사용할 일 없으니 전부 dto 조회하는 게 난듯)
    public List<CategoryBestItemQueryResponse> findCategoryBestItemWithPaging(List<Long> categoryIds, CustomPageRequest customPageRequest) {

        List<CategoryBestItemQueryResponse> categoryBestItemQueryResponses = queryFactory.select(new QCategoryBestItemQueryResponse(item.id, colorItem.id, item.name, item.price, item.discountedPrice)).from(orderItem)
                .join(orderItem.colorItemSizeStock, colorItemSizeStock).join(colorItemSizeStock.colorItem, colorItem).join(colorItem.item, item)
                .where(categoryIdIn(categoryIds))
                .groupBy(colorItem.id)
                .orderBy(orderItem.count.sum().desc())
                .offset(customPageRequest.getPage() - 1)
                .limit(customPageRequest.getSize())
                .fetch();

        //이미지 세팅
        List<Long> colorItemIds = categoryBestItemQueryResponses.stream().map(CategoryBestItemQueryResponse::getColorItemId).toList();

        //1차캐시 재사용은 findById만 되서 map 씀
        List<ColorItemImage> colorItemImages = colorItemImageRepository.findAllByColorItemIdInAndSequence(colorItemIds, 1);

        Map<Long, UploadFile> uploadFileMap = colorItemImages.stream().collect(Collectors.toMap(c -> c.getColorItem().getId(), ColorItemImage::getUploadFile));
        categoryBestItemQueryResponses.forEach(cbi -> cbi.setUploadFile(
                uploadFileMap.get(cbi.getColorItemId())
        ));

        //리뷰 개수 세팅
        List<CategoryBestItemReviewQueryDto> reviewQueryDtos = findAvgRatingsCountInColorItemId(colorItemIds);
        Map<Long, CategoryBestItemReviewQueryDto> reviewMap = reviewQueryDtos.stream()
                .collect(Collectors.toMap(CategoryBestItemReviewQueryDto::getColorItemId, Function.identity()));

        categoryBestItemQueryResponses.forEach(cbi -> {
                    CategoryBestItemReviewQueryDto reviewQueryDto = reviewMap.get(cbi.getColorItemId());
                    if (reviewQueryDto != null) {
                        cbi.setAvgRating(reviewQueryDto.getAvgRating());
                        cbi.setReviewCount(reviewQueryDto.getReviewCount());
                    }
                }
        );

        return categoryBestItemQueryResponses;
    }

    //이상 없지만, IDE 에러 없애려고 cast 적용
    private List<CategoryBestItemReviewQueryDto> findAvgRatingsCountInColorItemId(List<Long> colorItemIds) {
        String jpql = "select new org.example.tamaapi.repository.item.query.dto.CategoryBestItemReviewQueryDto(ci.id, CAST(ROUND(AVG(r.rating), 1) AS double), count(ci.id)) from Review r" +
                " join r.orderItem oi join oi.colorItemSizeStock isk join isk.colorItem ci where ci.id in :colorItemIds" +
                " group by ci.id";
        TypedQuery<CategoryBestItemReviewQueryDto> query = em.createQuery(jpql, CategoryBestItemReviewQueryDto.class);
        query.setParameter("colorItemIds", colorItemIds);
        return query.getResultList();
    }

    //--------------------------------------------------------------------------------------------------------------------------------------------------------

    private OrderSpecifier<?> categoryItemSort(CustomSort sort) {
        Order direction = sort.getDirection().isAscending() ? Order.ASC : Order.DESC;
        return switch (sort.getProperty()) {
            case "price" ->
                    new OrderSpecifier<>(direction, Expressions.numberTemplate(Integer.class, "coalesce({0}, {1})", item.discountedPrice, item.price));
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

    private BooleanExpression itemIdIn(List<Long> itemIds) {
        return isEmpty(itemIds) ? null : item.id.in(itemIds);
    }

}
