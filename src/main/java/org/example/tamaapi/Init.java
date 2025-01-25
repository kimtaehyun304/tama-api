package org.example.tamaapi;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.domain.*;
import org.example.tamaapi.repository.*;
import org.example.tamaapi.repository.ItemImageRepository;
import org.example.tamaapi.repository.ItemRepository;
import org.example.tamaapi.repository.ItemStockRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class Init {

    private final InitService initService;

    @PostConstruct
    public void init() throws InterruptedException {
        initService.initCategory();
        initService.initColor();
        initService.initItem();
        initService.initMember();
    }

    @Component
    //@Transactional
    @RequiredArgsConstructor
    static class InitService {

        private final ItemRepository itemRepository;
        private final ColorItemRepository colorItemRepository;
        private final ItemStockRepository itemStockRepository;
        private final CategoryRepository categoryRepository;
        private final ItemImageRepository itemImageRepository;
        private final MemberRepository memberRepository;
        private final BCryptPasswordEncoder bCryptPasswordEncoder;
        private final ColorRepository colorRepository;

        public void initCategory() {
            Category outer = Category.builder().name("아우터").build();
            categoryRepository.save(outer);

            Category downPadding = Category.builder().name("다운/패딩").parent(outer).build();
            categoryRepository.save(downPadding);

            Category jacketCoat = Category.builder().name("자켓/코트").parent(outer).build();
            categoryRepository.save(jacketCoat);

            Category vest = Category.builder().name("베스트").parent(outer).build();
            categoryRepository.save(vest);

            Category jumper = Category.builder().name("점퍼").parent(outer).build();
            categoryRepository.save(jumper);

            Category top = Category.builder().name("상의").build();
            categoryRepository.save(top);

            Category tShirt = Category.builder().name("티셔츠").parent(top).build();
            categoryRepository.save(tShirt);

            Category knitCardigan = Category.builder().name("니트/가디건").parent(top).build();
            categoryRepository.save(knitCardigan);

            Category shirt = Category.builder().name("셔츠").parent(top).build();
            categoryRepository.save(shirt);

            Category Blouse = Category.builder().name("블라우스").parent(top).build();
            categoryRepository.save(Blouse);

            Category sweat = Category.builder().name("스웨트").parent(top).build();
            categoryRepository.save(sweat);

            Category bottom = Category.builder().name("하의").build();
            categoryRepository.save(bottom);

            Category denimPants = Category.builder().name("데님팬츠").parent(bottom).build();
            categoryRepository.save(denimPants);

            Category pants = Category.builder().name("팬츠").parent(bottom).build();
            categoryRepository.save(pants);

            Category sweatActive = Category.builder().name("스웨트/액티브").parent(bottom).build();
            categoryRepository.save(sweatActive);

            Category skirt = Category.builder().name("스커트").parent(bottom).build();
            categoryRepository.save(skirt);
        }

        public void initColor() {
            Color white = Color.builder().name("화이트").hexCode("#FFFFFF").build();
            colorRepository.save(white);

            Color gray = Color.builder().name("그레이").hexCode("#BFBFBF").build();
            colorRepository.save(gray);

            Color black = Color.builder().name("블랙").hexCode("#000000").build();
            colorRepository.save(black);

            Color red = Color.builder().name("레드").hexCode("#E30718").build();
            colorRepository.save(red);

            Color brown = Color.builder().name("브라운").hexCode("#A76A33").build();
            colorRepository.save(brown);

            Color yellow = Color.builder().name("옐로우").hexCode("#F2E646").build();
            colorRepository.save(yellow);

            Color green = Color.builder().name("그린").hexCode("#6AB441").build();
            colorRepository.save(green);

            Color blue = Color.builder().name("블루").hexCode("#4B7EB7").build();
            colorRepository.save(blue);

            Color navy = Color.builder().name("네이비").hexCode("#000080").parent(blue).build();
            colorRepository.save(navy);

            Color pink = Color.builder().name("핑크").hexCode("#FFC0CB").parent(red).build();
            colorRepository.save(pink);

            Color beige = Color.builder().name("베이지").hexCode("#F5F5DC").parent(white).build();
            colorRepository.save(beige);

            Color ivory = Color.builder().name("아이보리").hexCode("#FFFFF0").parent(white).build();
            colorRepository.save(ivory);

        }


        public void initItem() {
            List<ColorItemRequest> colorItems = Arrays.asList(
                    new ColorItemRequest(
                            colorRepository.findByName("아이보리").get(),
                            "/woman-ivory-pants.jpg",
                            Arrays.asList("/woman-ivory-pants-detail.jpg", "/woman-ivory-pants-detail2.jpg"),
                            Arrays.asList(
                                    new SizeStockRequest("S(67CM)", 1),
                                    new SizeStockRequest("M(70CM)", 2)
                            )
                    ),
                    new ColorItemRequest(
                            colorRepository.findByName("핑크").get(),
                            "/woman-pink-pants.jpg",
                            Arrays.asList("/woman-pink-pants-detail.jpg", "/woman-pink-pants-detail2.jpg"),
                            Arrays.asList(
                                    new SizeStockRequest("S(67CM)", 2),
                                    new SizeStockRequest("M(70CM)", 2)
                            )
                    )
            );

            createItem(
                    "데님팬츠",
                    49900,
                    39900,
                    Gender.FEMALE,
                    "24 F/W",
                    "여 코듀로이 와이드 팬츠",
                    "무형광 원단입니다. 전 년 상품 자주히트와 동일한 소재이며, 네이밍이변경되었습니다.",
                    "2024-08-01",
                    "방글라데시",
                    "(주)신세계인터내셔날",
                    "폴리에스터 94%, 폴리우레탄 6% (상표,장식,무늬,자수,밴드,심지,보강재 제외)",
                    "세제는 중성세제를 사용하고 락스 등의 표백제는 사용을 금합니다. 세탁 시 삶아 빨 경우 섬유의 특성이 소멸되어 수축 및 물빠짐의 우려가 있으므로 미온 세탁하시기 바랍니다.",
                    colorItems
            );

            colorItems = Arrays.asList(
                    new ColorItemRequest(
                            colorRepository.findByName("블루").get(),
                            "/man-blue-pants.jpg",
                            Arrays.asList("/man-blue-pants-detail.jpg", "/man-blue-pants-detail2.jpg"),
                            Arrays.asList(
                                    new SizeStockRequest("S(70CM)", 1),
                                    new SizeStockRequest("M(80CM)", 2)
                            )
                    ),
                    new ColorItemRequest(
                            colorRepository.findByName("네이비").get(),
                            "/man-navy-pants.jpg",
                            Arrays.asList("/man-navy-pants-detail.jpg", "/man-navy-pants-detail2.jpg"),
                            Arrays.asList(
                                    new SizeStockRequest("S(70CM)", 0),
                                    new SizeStockRequest("M(80CM)", 0)
                            )
                    )
            );

            createItem(
                    "데님팬츠",
                    49900,
                    29900,
                    Gender.MALE,
                    "24 F/W",
                    "남 데님 밴딩 팬츠",
                    "데님 염색 특성상 마찰에 의해 밝은 색상의 다른 제품 (의류, 운동화, 가방, 소파, 자동차 시트 등) 및 가죽류에 이염 될 수 있으니 주의하여 주시고, 단독 손세탁 및 건조하시기 바랍니다.",
                    "2024-07-01",
                    "중국",
                    "(주)신세계인터내셔날",
                    "겉감 - 면 91%, 폴리에스터 7%, 폴리우레탄 2%",
                    "상품별 정확한 세탁방법은 세탁취급주의 라벨을 확인한 뒤 세탁 바랍니다.",
                    colorItems
            );

        }

        public void initMember(){
            String password = bCryptPasswordEncoder.encode("qwer123456");
            Member member = Member.builder().email("burnaby033@naver.com").password(password).build();
            memberRepository.save(member);
        }

        public void createItem(
                String categoryName,
                int price,
                int discountedPrice,
                Gender gender,
                String yearSeason,
                String name,
                String description,
                String dateOfManufacture,
                String countryOfManufacture,
                String manufacturer,
                String textile,
                String precaution,
                List<ColorItemRequest> colorItems
        ) {
            Category category = categoryRepository.findByName(categoryName)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryName));

            Item item = Item.builder()
                    .category(category)
                    .price(price)
                    .discountedPrice(discountedPrice)
                    .gender(gender)
                    .yearSeason(yearSeason)
                    .name(name)
                    .description(description)
                    .dateOfManufacture(LocalDate.parse(dateOfManufacture))
                    .countryOfManufacture(countryOfManufacture)
                    .manufacturer(manufacturer)
                    .textile(textile)
                    .precaution(precaution)
                    .build();

            itemRepository.save(item);

            for (ColorItemRequest colorItemRequest : colorItems) {
                ColorItem colorItem = ColorItem.builder()
                        .item(item)
                        .color(colorItemRequest.getColor())
                        .imageSrc(colorItemRequest.getImageSrc())
                        .build();
                colorItemRepository.save(colorItem);

                for (String imageSrc : colorItemRequest.getImageDetails()) {
                    ItemImage itemImage = ItemImage.builder()
                            .colorItem(colorItem)
                            .src(imageSrc)
                            .build();
                    itemImageRepository.save(itemImage);
                }

                for (SizeStockRequest sizeStock : colorItemRequest.getSizeStocks()) {
                    ItemStock itemStock = ItemStock.builder()
                            .colorItem(colorItem)
                            .size(sizeStock.getSize())
                            .stock(sizeStock.getStock())
                            .build();
                    itemStockRepository.save(itemStock);
                }
            }
        }



    }

    static class ColorItemRequest {
        private Color color;
        private String imageSrc;
        private List<String> imageDetails;
        private List<SizeStockRequest> sizeStocks;

        // 생성자 및 getter/setter
        public ColorItemRequest(Color color, String imageSrc, List<String> imageDetails, List<SizeStockRequest> sizeStocks) {
            this.color = color;
            this.imageSrc = imageSrc;
            this.imageDetails = imageDetails;
            this.sizeStocks = sizeStocks;
        }

        public Color getColor() { return color; }
        public String getImageSrc() { return imageSrc; }
        public List<String> getImageDetails() { return imageDetails; }
        public List<SizeStockRequest> getSizeStocks() { return sizeStocks; }
    }

    static class SizeStockRequest {
        private String size;
        private int stock;

        // 생성자 및 getter/setter
        public SizeStockRequest(String size, int stock) {
            this.size = size;
            this.stock = stock;
        }

        public String getSize() { return size; }
        public int getStock() { return stock; }
    }

}