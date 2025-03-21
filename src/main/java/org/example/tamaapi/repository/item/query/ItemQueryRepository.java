package org.example.tamaapi.repository.item.query;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.example.tamaapi.domain.Gender;
import org.example.tamaapi.domain.item.ColorItem;
import org.example.tamaapi.domain.item.ColorItemImage;
import org.example.tamaapi.domain.item.Item;
import org.example.tamaapi.dto.UploadFile;
import org.example.tamaapi.dto.requestDto.CustomPageRequest;
import org.example.tamaapi.dto.requestDto.MySort;
import org.example.tamaapi.dto.responseDto.CustomPage;
import org.example.tamaapi.dto.responseDto.category.item.CategoryBestItemResponse;
import org.example.tamaapi.dto.responseDto.category.item.CategoryItemResponse;
import org.example.tamaapi.dto.responseDto.category.item.RelatedColorItemResponse;
import org.example.tamaapi.exception.MyBadRequestException;
import org.example.tamaapi.repository.item.ColorItemImageRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.example.tamaapi.util.ErrorMessageUtil.NOT_FOUND_IMAGE;

//DTO 반환이라 extends 엔티티에 맞는게 없음. -> 아무거나 써도 에러 안남. 무난하게 루트 엔티티 적어둠
//DATA JPA 안쓰는 게 어울리나, 순수 JPA는 생산성이 낮아서 안쓰기로 함
//그럴라 했는데 동적 쿼리 있어서 게획 변경
@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemQueryRepository {

    private final EntityManager em;
    private final ColorItemImageRepository colorItemImageRepository;

    public CustomPage<CategoryItemQueryDto> findCategoryItemsByFilter(MySort sort, CustomPageRequest customPageRequest, List<Long> categoryIds, Integer minPrice, Integer maxPrice, List<Long> colorIds, List<Gender> genders, Boolean isContainSoldOut) {
        //다용도
        List<Item> items = findItemsByCategoryIdInAndFilter(categoryIds, minPrice, maxPrice, colorIds, genders, isContainSoldOut);

        //페이징
        List<Long> itemIds = items.stream().map(Item::getId).toList();
        List<CategoryItemQueryDto> pagingCategoryItems = findAllByItemIdIn(itemIds, sort, customPageRequest);

        //자식 컬렉션 (해당 페이지 자식만)
        List<Long> pagedItemIds = pagingCategoryItems.stream().map(CategoryItemQueryDto::getItemId).toList();
        List<RelatedColorItemResponse> colorItems = fetchColorItemsByCategoryIdInAndFilter(pagedItemIds, colorIds, isContainSoldOut);

        //커스텀 페이징 변환
        int rowCount = items.size();
        CustomPage<CategoryItemQueryDto> customPagingCategoryItems = new CustomPage<>(pagingCategoryItems, customPageRequest, rowCount);

        //key:itemId. List<CategoryItemResponse>에 List<RelatedColorItemResponse> 삽입
        Map<Long, List<RelatedColorItemResponse>> colorItemMap = colorItems.stream().collect(Collectors.groupingBy(RelatedColorItemResponse::getItemId));
        customPagingCategoryItems.getContent().forEach(ci -> ci.setRelatedColorItems(colorItemMap.get(ci.getItemId())));
        return customPagingCategoryItems;
    }


    //--카테고리 아이템 로직 시작
    //페이징 where in 절에 쓸 itemIds, rowCount
    //페이징 자식 컬렉션에 쓸 colorItemIds(지연 로딩) //이건 뭐지?
    private List<Item> findItemsByCategoryIdInAndFilter(List<Long> categoryIds, Integer minPrice, Integer maxPrice, List<Long> colorIds, List<Gender> genders, Boolean isContainSoldOut) {
        String jpql = "SELECT i FROM Item i JOIN i.colorItems ci JOIN ci.colorItemSizeStocks s JOIN ci.color WHERE i.category.id IN :categoryIds";

        // WHERE
        if (minPrice != null) jpql += " AND COALESCE(i.discountedPrice, i.price) >= :minPrice";
        if (maxPrice != null) jpql += " AND COALESCE(i.discountedPrice, i.price) <= :maxPrice";
        if (colorIds != null && !colorIds.isEmpty()) jpql += " AND ci.color.id IN :colorIds";
        if (genders != null && !genders.isEmpty()) jpql += " AND i.gender IN :genders";
        if (isContainSoldOut == null || Boolean.FALSE.equals(isContainSoldOut)) jpql += " AND s.stock > 0";

        // 그룹화 추가 (안 필요하길랙 주석)
        //jpql += " GROUP BY i.id, ci.id";

        TypedQuery<Item> query = em.createQuery(jpql, Item.class);

        query.setParameter("categoryIds", categoryIds);
        if (minPrice != null) query.setParameter("minPrice", minPrice);
        if (maxPrice != null) query.setParameter("maxPrice", maxPrice);
        if (colorIds != null && !colorIds.isEmpty()) query.setParameter("colorIds", colorIds);
        if (genders != null && !genders.isEmpty()) query.setParameter("genders", genders);

        return query.getResultList();
    }

    //페이징, 정렬
    //다대일 조인 여러번해서 페이징하면 colorItems 리스트가 이상하게 들어가서 이렇게 함
    private List<CategoryItemQueryDto> findAllByItemIdIn(List<Long> itemIds, MySort sort, CustomPageRequest customPageRequest) {
        String jpql = "SELECT new org.example.tamaapi.repository.item.query.CategoryItemQueryDto(i) FROM Item i WHERE i.id in :itemIds";

        // ORDER BY. 컨트롤러 sort required true -> sort null X
        switch (sort.getProperty()) {
            case "price" ->
                    jpql += String.format(" order by COALESCE(i.discountedPrice, i.price) %s, i.id DESC", sort.getDirection());
            case "createdAt" -> jpql += " order by i.createdAt DESC, i.id DESC";
            default -> throw new MyBadRequestException("유효한 property가 없습니다.");
        }

        TypedQuery<CategoryItemQueryDto> query = em.createQuery(jpql, CategoryItemQueryDto.class);
        query.setParameter("itemIds", itemIds);

        query.setFirstResult((customPageRequest.getPage() - 1) * customPageRequest.getSize());
        query.setMaxResults(customPageRequest.getSize());
        return query.getResultList();
    }

    //페이징 아이템 자식 컬렉션. 컨트롤러에서 루프 돌며 페이징 객체에 삽입
    //페이징 쿼리는 colorItems 필터링이 날라가서, 다시 필터링해야함
    private List<RelatedColorItemResponse> fetchColorItemsByCategoryIdInAndFilter(List<Long> itemIds, List<Long> colorIds, Boolean isContainSoldOut) {

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

        List<RelatedColorItemResponse> relatedColorItems = query.getResultList();

        //이미지 세팅
        List<Long> colorItemIds = relatedColorItems.stream().map(RelatedColorItemResponse::getColorItemId).toList();

        //1차캐시 재사용은 findById만 되서 map 씀
        List<ColorItemImage> colorItemImages = colorItemImageRepository.findAllByColorItemIdInAndSequence(colorItemIds, 1);
        Map<Long, UploadFile> uploadFileMap = colorItemImages.stream().collect(Collectors.toMap(c -> c.getColorItem().getId(), ColorItemImage::getUploadFile));
        relatedColorItems.forEach(rci -> rci.setUploadFile(
                uploadFileMap.get(rci.getColorItemId())
        ));

        return relatedColorItems;
    }

    //가격 최소값, 최대값. 페이징 쿼리랑 별도로 요청되서 in 절 못 씀. 필터 필요
    //group by 불필요
    public Optional<ItemMinMaxQueryDto> findMinMaxPriceByCategoryIdInAndFilter(List<Long> categoryIds, Integer minPrice, Integer maxPrice, List<Long> colorIds, List<Gender> genders, Boolean isContainSoldOut) {

        String jpql = "SELECT new org.example.tamaapi.repository.item.query.ItemMinMaxQueryDto(MIN(COALESCE(i.discountedPrice, i.price)), MAX(COALESCE(i.discountedPrice, i.price))) FROM ColorItem ci " +
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


    //--카테고리 베스트 아이템 시작
    //DTO 조회는 JOIN FETCH X,  생성자 파라미터는 SELECT절과 동등, GROUP BY는 SELECE절에 명시된것만 가능
    //이거 왜 dto 조회로 했지? -> orderItem 루트라 바꿀려고
    public List<CategoryBestItemQueryDto> findCategoryBestItemByCategoryIdIn(List<Long> categoryIds, CustomPageRequest customPageRequest) {
        String jpql = "select new org.example.tamaapi.repository.item.query.CategoryBestItemQueryDto(ci, i) from OrderItem" +
                " oi join oi.colorItemSizeStock ciss join ciss.colorItem ci join ci.item i";

        if (!categoryIds.isEmpty())
            jpql += " where i.category.id in :categoryIds";

        //판매수로 정렬.
        jpql += " group by ci.id order by count(ci.id) desc";

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

        //리뷰 세팅
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

    private List<CategoryBestItemReviewQueryDto> findAvgRatingsCountInColorItemId(List<Long> colorItemIds) {
        String jpql = "select new org.example.tamaapi.repository.item.query.CategoryBestItemReviewQueryDto(ci.id, avg(r.rating), count(ci.id)) from Review r" +
                " join r.colorItemSizeStock isk join isk.colorItem ci where ci.id in :colorItemIds" +
                " group by ci.id";
        TypedQuery<CategoryBestItemReviewQueryDto> query = em.createQuery(jpql, CategoryBestItemReviewQueryDto.class);
        query.setParameter("colorItemIds", colorItemIds);
        return query.getResultList();
    }
    //--카테고리 베스트 아이템 끝
}
