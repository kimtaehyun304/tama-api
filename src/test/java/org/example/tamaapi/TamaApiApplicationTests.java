package org.example.tamaapi;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.example.tamaapi.domain.Gender;
import org.example.tamaapi.dto.requestDto.CustomPageRequest;
import org.example.tamaapi.dto.requestDto.CustomSort;
import org.example.tamaapi.exception.MyBadRequestException;
import org.example.tamaapi.repository.item.ColorItemImageRepository;
import org.example.tamaapi.repository.item.query.dto.QCategoryItemQueryDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;

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

    @Test
    public void 서브쿼리_페이징(){
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


        long execStart, execEnd;

        execStart = System.currentTimeMillis();
        queryFactory
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
         execEnd = System.currentTimeMillis();
        System.out.println("[서브쿼리 페이징] 걸린 시간: " + (execEnd - execStart) + " ms");
    }

    @Test
    public void groupBy_페이징(){
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


        long execStart, execEnd;
        execStart = System.currentTimeMillis();
        queryFactory
                .select(new QCategoryItemQueryDto(item.id, item.name, item.price, item.discountedPrice)).from(item)
                .join(item.colorItems, colorItem).join(colorItem.colorItemSizeStocks, colorItemSizeStock).join(colorItem.color, color)
                .where(categoryIdIn(categoryIds), itemNameContains(itemName), minPriceGoe(minPrice), maxPriceLoe(maxPrice), colorIdIn(colorIds), genderIn(genders), isContainSoldOut(isContainSoldOut))
                .groupBy(item.id)
                .offset(customPageRequest.getPage() - 1)
                .limit(customPageRequest.getSize())
                .orderBy(categoryItemSort(sort), new OrderSpecifier<>(Order.DESC, item.id))
                .fetch();
        execEnd = System.currentTimeMillis();
        System.out.println("[groupBy 페이징] 걸린 시간: " + (execEnd - execStart) + " ms");
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
