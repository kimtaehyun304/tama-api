package org.example.tamaapi;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.example.tamaapi.domain.Gender;
import org.example.tamaapi.dto.requestDto.CustomPageRequest;
import org.example.tamaapi.dto.requestDto.CustomSort;
import org.example.tamaapi.exception.MyBadRequestException;
import org.example.tamaapi.repository.item.ColorItemImageRepository;
import org.example.tamaapi.repository.item.query.dto.CategoryBestItemQueryResponse;
import org.example.tamaapi.repository.item.query.dto.QCategoryItemQueryDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.example.tamaapi.domain.item.QCategory.category;
import static org.example.tamaapi.domain.item.QColor.color;
import static org.example.tamaapi.domain.item.QColorItem.colorItem;
import static org.example.tamaapi.domain.item.QColorItemSizeStock.colorItemSizeStock;
import static org.example.tamaapi.domain.item.QItem.item;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.util.StringUtils.hasText;

@SpringBootTest
class TamaApiApplicationTests {

    @Autowired
    private JPAQueryFactory queryFactory;
    private EntityManager em;

    //페이징 sql에는 일대다 조인 불가 → 두 해결 방법 존재
    // ○ where절에 서브 쿼리 사용
    // ○ groupBy로 행을 줄이고 페이징 처리

    // 두 방법 속도 비교 결과
    // ○ System.currentTimeMillis() 비교 : 둘 다 비슷함
    // ○ mysql explain 비교 : groupBy 승리

    //where절에 서브 쿼리 사용
    @Test
    public void 서브쿼리(){
        //요청 파라미터
        CustomPageRequest customPageRequest = new CustomPageRequest(1, 10);
        List<Long> categoryIds = new ArrayList<>();
        String itemName = "";
        Integer minPrice = null;
        Integer maxPrice = null;
        List<Long> colorIds = new ArrayList<>();
        List<Gender> genders = new ArrayList<>();
        Boolean isContainSoldOut = true;
        CustomSort sort = new CustomSort("price", Sort.Direction.DESC);

        long startTime, endTime;
        startTime = System.currentTimeMillis();

        queryFactory
                .select(new QCategoryItemQueryDto(item.id, item.name, item.originalPrice, item.nowPrice)).from(item)
                .where(item.id.in(
                        JPAExpressions
                                .select(item.id).distinct().from(item)
                                .join(item.colorItems, colorItem).join(colorItem.colorItemSizeStocks, colorItemSizeStock).join(colorItem.color, color)
                                .where(categoryIdIn(categoryIds), itemNameContains(itemName), minPriceGoe(minPrice), maxPriceLoe(maxPrice), colorIdIn(colorIds), genderIn(genders), isContainSoldOut(isContainSoldOut))))
                .offset(customPageRequest.getPage() - 1)
                .limit(customPageRequest.getSize())
                .orderBy(categoryItemSort(sort), new OrderSpecifier<>(Order.DESC, item.id))
                .fetch();

        endTime = System.currentTimeMillis();
        System.out.println("[서브쿼리 페이징] 걸린 시간: " + (endTime - startTime) + " ms");
    }

    //groupBy로 행을 줄이고 페이징 처리
    @Test
    public void groupBy(){
        //요청 파라미터
        CustomPageRequest customPageRequest = new CustomPageRequest(1, 10);
        List<Long> categoryIds = new ArrayList<>();
        String itemName = "";
        Integer minPrice = null;
        Integer maxPrice = null;
        List<Long> colorIds = new ArrayList<>();
        List<Gender> genders = new ArrayList<>();
        Boolean isContainSoldOut = true;
        CustomSort sort = new CustomSort("price", Sort.Direction.DESC);

        long startTime, endTime;
        startTime = System.currentTimeMillis();

        queryFactory
                .select(new QCategoryItemQueryDto(item.id, item.name, item.originalPrice, item.nowPrice)).from(item)
                .join(item.colorItems, colorItem).join(colorItem.colorItemSizeStocks, colorItemSizeStock).join(colorItem.color, color)
                .where(categoryIdIn(categoryIds), itemNameContains(itemName), minPriceGoe(minPrice), maxPriceLoe(maxPrice), colorIdIn(colorIds), genderIn(genders), isContainSoldOut(isContainSoldOut))
                .groupBy(item.id)
                .offset(customPageRequest.getPage() - 1)
                .limit(customPageRequest.getSize())
                .orderBy(categoryItemSort(sort), new OrderSpecifier<>(Order.DESC, item.id))
                .fetch();

        endTime = System.currentTimeMillis();
        System.out.println("[groupBy 페이징] 걸린 시간: " + (endTime - startTime) + " ms");
    }

    //쿼리 튜닝 안한게 더 빠르길래 안 썼음
    //type all. using temporary인거 개선했는데 오히려 더 느림..
    @Test
    public void 인기상품_쿼리튜닝(){
        List<Long> categoryIds = new ArrayList<>(1);
        CustomPageRequest customPageRequest = new CustomPageRequest(1, 10);

        // 인덱스 적용을 위해 from절 칼럼을 gorupBy 칼럼과 일치 시켰음
        String sql = """
            SELECT STRAIGHT_JOIN i.item_id, c.color_item_id, i.name, i.original_price, i.now_price
            FROM color_item c
            JOIN color_item_size_stock cs ON cs.color_item_id = c.color_item_id
            JOIN order_item o ON o.color_item_size_stock_id = cs.color_item_size_stock_id
            JOIN item i  ON i.item_id = c.item_id      
        """;

        if(!CollectionUtils.isEmpty(categoryIds))
            sql += "and i.category_id IN (:categoryIds)";

        sql += """
                GROUP BY c.color_item_id
                ORDER BY SUM(o.count) DESC
                LIMIT :offset, :limit
            """;

        Query query = em.createNativeQuery(sql.toString());

        if(!CollectionUtils.isEmpty(categoryIds))
            query.setParameter("categoryIds", categoryIds);
        query.setParameter("offset", customPageRequest.getPage()-1);
        query.setParameter("limit", customPageRequest.getSize());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();

        List<CategoryBestItemQueryResponse> categoryBestItemQueryResponses = rows.stream()
                .map(row -> new CategoryBestItemQueryResponse(
                        ((Number) row[0]).longValue(),
                        ((Number) row[1]).longValue(),
                        (String) row[2],
                        row[3] != null ? ((Number) row[3]).intValue() : null,
                        row[4] != null ? ((Number) row[4]).intValue() : null
                ))
                .toList();

    }

    //using temporary 없앴는데 더 느려졌음
    @Test
    public void 상품검색_쿼리튜닝(){

        //요청 파라미터
        CustomPageRequest customPageRequest = new CustomPageRequest(1, 10);
        List<Long> categoryIds = new ArrayList<>();
        String itemName = "";
        Integer minPrice = null;
        Integer maxPrice = null;
        List<Long> colorIds = new ArrayList<>();
        List<Gender> genders = new ArrayList<>();
        Boolean isContainSoldOut = true;
        CustomSort sort = new CustomSort("price", Sort.Direction.DESC);

        queryFactory
                .select(new QCategoryItemQueryDto(item.id, item.name, item.originalPrice, item.nowPrice)).from(item)
                .where(genderIn(genders), minPriceGoe(minPrice), maxPriceLoe(maxPrice), itemNameContains(itemName),
                        JPAExpressions.selectOne()
                                .from(colorItem)
                                .join(colorItem.colorItemSizeStocks, colorItemSizeStock)
                                .where(colorIdIn(colorIds), isContainSoldOut(isContainSoldOut))
                                .exists()
                )
                .offset(customPageRequest.getPage() - 1)
                .limit(customPageRequest.getSize())
                .orderBy(categoryItemSort(sort))
                .fetch();

    }




    //--------------------------------------------------------------------------------------------------------------------------------------------------------
    //querydsl 조건문
    private OrderSpecifier<?> categoryItemSort(CustomSort sort) {
        Order direction = sort.getDirection().isAscending() ? Order.ASC : Order.DESC;
        return switch (sort.getProperty()) {
            //인데스 적용을 위해, 즉 coalesce 안 쓰기 위해 [price, discounted_price] -> [originalPrice, nowPrice]로 변경
            //case "price" -> new OrderSpecifier<>(direction, Expressions.numberTemplate(Integer.class, "coalesce({0}, {1})", item.discountedPrice, item.price));
            case "price" -> new OrderSpecifier<>(direction, item.nowPrice);

            //case "createdAt" -> new OrderSpecifier<>(Order.DESC, item.createdAt);
            //인데스 적용을 위해 item.id desc로 변경
            case "createdAt" -> new OrderSpecifier<>(Order.DESC, item.id);
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
        if (minPrice == null) return null;
        return item.nowPrice.goe(minPrice);
    }

    private BooleanExpression maxPriceLoe(Integer maxPrice) {
        if (maxPrice == null) return null;
        return item.nowPrice.loe(maxPrice);
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
