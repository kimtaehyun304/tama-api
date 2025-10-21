package org.example.tamaapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.example.tamaapi.domain.Gender;
import org.example.tamaapi.domain.item.ColorItem;
import org.example.tamaapi.domain.item.ColorItemSizeStock;
import org.example.tamaapi.domain.item.Item;
import org.example.tamaapi.domain.user.Authority;
import org.example.tamaapi.domain.user.Member;
import org.example.tamaapi.dto.requestDto.item.save.SaveColorItemRequest;
import org.example.tamaapi.dto.requestDto.item.save.SaveItemRequest;
import org.example.tamaapi.dto.requestDto.item.save.SaveSizeStockRequest;
import org.example.tamaapi.auth.jwt.TokenProvider;
import org.example.tamaapi.repository.MemberRepository;
import org.example.tamaapi.repository.item.ColorItemRepository;
import org.example.tamaapi.repository.item.ColorItemSizeStockRepository;
import org.example.tamaapi.repository.item.ItemRepository;
import org.example.tamaapi.util.ErrorMessageUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
// 테스트 끝나면 롤백 (auto_increment는 롤백 안됨)
@Transactional
class ItemApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ColorItemRepository colorItemRepository;

    @Autowired
    private ColorItemSizeStockRepository colorItemSizeStockRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityManager em;

    @Test
    void saveItems() throws Exception {
        //given
        String name = "여 코듀로이 스커트";
        Long categoryId = 15L;
        Integer originalPrice = 49900;
        Integer nowPrice = 49900;
        Gender gender = Gender.FEMALE;
        String description = "세로 방향의 얇은 골로 된 가벼운 코듀로이 소재의 스커트입니다.";
        String yearSeason = "25F/W";
        LocalDate dateOfManufacture = LocalDate.of(2025, 6, 1);
        String countryOfManufacture = "미얀마";
        String manufacturer = "(주)신세계인터내셔날";
        String textile = "면 63%, 폴리에스터 35%, 폴리우레탄 2%(상표,장식,무늬,자수,밴드,심지,보강재 제외)";
        String precaution = "1. 상품별 정확한 세탁방법은 세탁취급주의 라벨을 확인한 뒤 세탁 바랍니다.";

        List<SaveColorItemRequest> saveColorItemRequests = new ArrayList<>();

        //아이보리
        saveColorItemRequests.add(new SaveColorItemRequest(3L,
                List.of(
                        new SaveSizeStockRequest("S(67CM)", 10),
                        new SaveSizeStockRequest("M(70CM)", 10)
                )));

        //챠콜
        saveColorItemRequests.add(new SaveColorItemRequest(4L,
                List.of(
                        new SaveSizeStockRequest("S(67CM)", 10),
                        new SaveSizeStockRequest("M(70CM)", 10)
                )));

        SaveItemRequest request = new SaveItemRequest(name, categoryId, originalPrice, nowPrice
                , gender, description, yearSeason, dateOfManufacture, countryOfManufacture, manufacturer
                , textile, precaution, saveColorItemRequests);

        // when
        Member admin = memberRepository.findAllByAuthority(Authority.ADMIN).get(0);
        String accessToken = tokenProvider.generateToken(admin);
        mockMvc.perform(post("/api/items/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.savedColorItemIds", hasSize(2)));
        //@Transactional 때문에 mockMvc랑 1차 캐시 공유해서 클리어 해야함.
        //저장할때 pk만 채우고 연관관게 연결 안해놈
        em.clear();

        //then
        Item item = itemRepository.findWithColorItemByName(name)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessageUtil.NOT_FOUND_ITEM));
        assertThat(item.getName()).isEqualTo(name);
        assertThat(item.getCategory().getId()).isEqualTo(categoryId);
        assertThat(item.getOriginalPrice()).isEqualTo(originalPrice);
        assertThat(item.getNowPrice()).isEqualTo(nowPrice);
        assertThat(item.getGender()).isEqualTo(gender);
        assertThat(item.getDescription()).isEqualTo(description);
        assertThat(item.getYearSeason()).isEqualTo(yearSeason);
        assertThat(item.getDateOfManufacture()).isEqualTo(dateOfManufacture);
        assertThat(item.getCountryOfManufacture()).isEqualTo(countryOfManufacture);
        assertThat(item.getManufacturer()).isEqualTo(manufacturer);
        assertThat(item.getTextile()).isEqualTo(textile);
        assertThat(item.getPrecaution()).isEqualTo(precaution);

        List<SaveColorItemRequest> savedColorItemRequests = new ArrayList<>();

        List<ColorItem> colorItems = colorItemRepository.findAllByItemId(item.getId());
        List<Long> colorItemIds = colorItems.stream().map(ColorItem::getId).toList();
        List<ColorItemSizeStock> colorItemSizeStocks = colorItemSizeStockRepository.findAllByColorItemIdIn(colorItemIds);
        //key:colorId
        Map<Long, List<ColorItemSizeStock>> map = colorItemSizeStocks
                .stream().collect(Collectors.groupingBy(c -> c.getColorItem().getColor().getId()));

        for (ColorItem colorItem : colorItems) {
            savedColorItemRequests.add(
                    new SaveColorItemRequest(colorItem.getColor().getId(),
                            map.get(colorItem.getColor().getId())
                                    .stream().map(c -> new SaveSizeStockRequest(c.getSize(), c.getStock())).toList())
            );
        }

        assertThat(saveColorItemRequests)
                .usingRecursiveComparison()
                .isEqualTo(savedColorItemRequests);
    }


}