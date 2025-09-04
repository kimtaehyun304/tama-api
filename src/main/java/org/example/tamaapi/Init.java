package org.example.tamaapi;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.cache.BestItem;
import org.example.tamaapi.cache.MyCacheType;
import org.example.tamaapi.domain.*;
import org.example.tamaapi.domain.item.*;
import org.example.tamaapi.domain.order.Delivery;
import org.example.tamaapi.domain.order.Order;
import org.example.tamaapi.domain.order.OrderItem;
import org.example.tamaapi.domain.user.*;
import org.example.tamaapi.dto.UploadFile;
import org.example.tamaapi.dto.requestDto.CustomPageRequest;
import org.example.tamaapi.dto.requestDto.order.SaveGuestOrderRequest;
import org.example.tamaapi.dto.requestDto.order.SaveMemberOrderRequest;
import org.example.tamaapi.dto.requestDto.order.SaveOrderItemRequest;
import org.example.tamaapi.repository.*;
import org.example.tamaapi.repository.item.*;
import org.example.tamaapi.repository.item.query.ItemQueryRepository;
import org.example.tamaapi.repository.item.query.dto.CategoryBestItemQueryResponse;
import org.example.tamaapi.repository.order.DeliveryRepository;
import org.example.tamaapi.repository.order.OrderItemRepository;
import org.example.tamaapi.repository.order.OrderRepository;
import org.example.tamaapi.service.CacheService;
import org.example.tamaapi.service.ItemService;
import org.example.tamaapi.service.MemberService;
import org.example.tamaapi.service.ReviewService;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class Init {

    private final InitService initService;
    private final Environment environment;

    @PostConstruct
    public void init() {
        String ddlAuto = environment.getProperty("spring.jpa.hibernate.ddl-auto", "none");

        if(!ddlAuto.equals("none"))
            initService.initAll();

        //캐시 메모리에 올려두는 거라 매번 초기화 해야함
        initService.initBestItemCache();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {

        private final ColorItemSizeStockRepository colorItemSizeStockRepository;
        private final CategoryRepository categoryRepository;
        private final MemberRepository memberRepository;
        private final BCryptPasswordEncoder bCryptPasswordEncoder;
        private final ColorRepository colorRepository;
        private final ReviewRepository reviewRepository;
        private final ReviewService reviewService;
        private final OrderRepository orderRepository;
        private final JdbcTemplateRepository jdbcTemplateRepository;
        private final MemberService memberService;
        private final ItemService itemService;
        private final OrderItemRepository orderItemRepository;
        private final ItemRepository itemRepository;
        private final ColorItemRepository colorItemRepository;
        private final MemberAddressRepository memberAddressRepository;
        private final DeliveryRepository deliveryRepository;
        private final ItemQueryRepository itemQueryRepository;
        private final CacheService cacheService;

        public boolean isNotInit() {
            return colorItemSizeStockRepository.count() == 0 &&
                    categoryRepository.count() == 0 &&
                    memberRepository.count() == 0 &&
                    colorRepository.count() == 0 &&
                    reviewRepository.count() == 0 &&
                    orderRepository.count() == 0 &&
                    orderItemRepository.count() == 0;
        }

        public void initAll() {
            initCategory();
            initColor();
            initMember();
            initMemberAddress();

            initManyItem(100000);
            initManyOrder(30000);
            initManyReview();

            initBestItemCache();
            /*
            initItem();
            initOrder();
            initReview();
             */

        }

        /*
        private void crawlItem(){

            String CATEGORY_NUMBER = "2502165830";

            RestClient.create().get()
                    .uri("https://www.shinsegaev.com/dispctg/initDispCtg.siv?disp_ctg_no={CATEGORY_NUMBER}&outlet_yn=N", CATEGORY_NUMBER)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new IllegalArgumentException("크롤링 실패");
                    })
                    .body(new ParameterizedTypeReference<>() {
                    });


            new Item("")
        }
         */

        private void initCategory() {
            Category outer = Category.builder().name("아우터").build();
            categoryRepository.save(outer);

            Category downPadding = Category.builder().name("다운/패딩").parent(outer).build();
            categoryRepository.save(downPadding);

            Category jacketCoat = Category.builder().name("자켓/코트/점퍼").parent(outer).build();
            categoryRepository.save(jacketCoat);

            Category vest = Category.builder().name("베스트").parent(outer).build();
            categoryRepository.save(vest);

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

        private void initColor() {
            //---
            Color white = Color.builder().name("화이트").hexCode("#FFFFFF").build();
            colorRepository.save(white);

            colorRepository.save(Color.builder().name("베이지").hexCode("#F5F5DC").parent(white).build());
            colorRepository.save(Color.builder().name("아이보리").hexCode("#FFFFF0").parent(white).build());

            //---
            Color gray = Color.builder().name("그레이").hexCode("#BFBFBF").build();
            colorRepository.save(gray);

            colorRepository.save(Color.builder().name("다크 그레이").hexCode("#363636").parent(gray).build());
            colorRepository.save(Color.builder().name("챠콜").hexCode("#36454F").parent(gray).build());

            //---
            Color black = Color.builder().name("블랙").hexCode("#000000").build();
            colorRepository.save(black);

            //---
            Color red = Color.builder().name("레드").hexCode("#E30718").build();
            colorRepository.save(red);

            colorRepository.save(Color.builder().name("핑크").hexCode("#FFC0CB").parent(red).build());
            colorRepository.save(Color.builder().name("브릭").hexCode("#A76A33").parent(red).build());

            //---
            Color brown = Color.builder().name("브라운").hexCode("#A76A33").build();
            colorRepository.save(brown);

            colorRepository.save(Color.builder().name("다크 브라운").hexCode("#291C13").parent(brown).build());
            colorRepository.save(Color.builder().name("다크 브라운").hexCode("#291C13").parent(brown).build());
            colorRepository.save(Color.builder().name("브릭").hexCode("#A76A33").parent(brown).build());

            //---
            Color yellow = Color.builder().name("옐로우").hexCode("#F2E646").build();
            colorRepository.save(yellow);

            colorRepository.save(Color.builder().name("라이트 옐로우").hexCode("#F5EA61").parent(yellow).build());

            //---
            Color green = Color.builder().name("그린").hexCode("#6AB441").build();
            colorRepository.save(green);

            colorRepository.save(Color.builder().name("카키").hexCode("#8F784B").parent(green).build());
            colorRepository.save(Color.builder().name("올리브").hexCode("#808000").parent(green).build());

            //---
            Color blue = Color.builder().name("블루").hexCode("#4B7EB7").build();
            colorRepository.save(blue);

            colorRepository.save(Color.builder().name("스카이 블루").hexCode("#87CEEB").parent(blue).build());
            colorRepository.save(Color.builder().name("네이비").hexCode("#000080").parent(blue).build());

            //---
            Color orange = Color.builder().name("오렌지").hexCode("#F89B00").build();
            colorRepository.save(orange);

            colorRepository.save(Color.builder().name("다크 오렌지").hexCode("#ff8c00").parent(orange).build());
        }

        private void initItem() {
            Category category = categoryRepository.findByName("팬츠").get();

            Item item = new Item(
                    49900,
                    39900,
                    Gender.FEMALE,
                    "24 F/W",
                    "여 코듀로이 와이드 팬츠",
                    "무형광 원단입니다. 전 년 상품 자주히트와 동일한 소재이며, 네이밍이변경되었습니다.",
                    LocalDate.parse("2024-08-01"),
                    "방글라데시",
                    "(주)신세계인터내셔날",
                    category,
                    "폴리에스터 94%, 폴리우레탄 6% (상표,장식,무늬,자수,밴드,심지,보강재 제외)",
                    "세제는 중성세제를 사용하고 락스 등의 표백제는 사용을 금합니다. 세탁 시 삶아 빨 경우 섬유의 특성이 소멸되어 수축 및 물빠짐의 우려가 있으므로 미온 세탁하시기 바랍니다.");

            List<ColorItem> colorItems = new ArrayList<>();
            List<ColorItemSizeStock> colorItemSizeStocks = new ArrayList<>();
            List<ColorItemImage> colorItemImages = new ArrayList<>();
            item.setCreatedAt(LocalDateTime.parse("2025-06-01T00:00:00"));

            // Color: 아이보리
            Color ivory = colorRepository.findByName("아이보리").get();
            ColorItem ivoryColorItem = new ColorItem(item, ivory);
            colorItems.add(ivoryColorItem);
            colorItemSizeStocks.addAll(List.of(new ColorItemSizeStock(ivoryColorItem, "S(67CM)", 9), new ColorItemSizeStock(ivoryColorItem, "M(67CM)", 9)));
            colorItemImages.addAll(
                    List.of(
                            new ColorItemImage(ivoryColorItem, new UploadFile("woman-ivory-pants.jpg", "woman-ivory-pants-uuid.jpg"), 1),
                            new ColorItemImage(ivoryColorItem, new UploadFile("woman-ivory-pants-detail.jpg", "woman-ivory-pants-detail-uuid.jpg"), 2)
                    )
            );

            // Color: 핑크
            Color pink = colorRepository.findByName("핑크").get();
            ColorItem pinkColorItem = new ColorItem(item, pink);
            colorItems.add(pinkColorItem);
            colorItemSizeStocks.addAll(List.of(new ColorItemSizeStock(pinkColorItem, "S(67CM)", 9), new ColorItemSizeStock(pinkColorItem, "M(67CM)", 9)));
            colorItemImages.addAll(List.of(
                            new ColorItemImage(pinkColorItem, new UploadFile("woman-pink-pants.jpg", "woman-pink-pants-uuid.jpg"), 1),
                            new ColorItemImage(pinkColorItem, new UploadFile("woman-pink-pants-detail.jpg", "woman-pink-pants-detail-uuid.jpg"), 2)
                    )
            );

            itemService.saveItem(item, colorItems, colorItemSizeStocks);
            itemService.saveColorItemImages(colorItemImages);

            colorItems.clear();
            colorItemSizeStocks.clear();
            colorItemImages.clear();
            //-------------------------------------------------------------------------------
            category = categoryRepository.findByName("데님팬츠").get();

            item = new Item(
                    49900,
                    29900,
                    Gender.MALE,
                    "24 F/W",
                    "남 데님 밴딩 팬츠",
                    "데님 염색 특성상 마찰에 의해 밝은 색상의 다른 제품 (의류, 운동화, 가방, 소파, 자동차 시트 등) 및 가죽류에 이염 될 수 있으니 주의하여 주시고, 단독 손세탁 및 건조하시기 바랍니다.",
                    LocalDate.parse("2024-07-01"),
                    "중국",
                    "(주)신세계인터내셔날",
                    category,
                    "겉감 - 면 91%, 폴리에스터 7%, 폴리우레탄 2%",
                    "상품별 정확한 세탁방법은 세탁취급주의 라벨을 확인한 뒤 세탁 바랍니다."
            );
            item.setCreatedAt(LocalDateTime.parse("2025-07-01T00:00:00"));

            // Color: Blue
            Color blue = colorRepository.findByName("블루").get();
            ColorItem blueColorItem = new ColorItem(item, blue);
            colorItems.add(blueColorItem);

            colorItemSizeStocks.addAll(List.of(
                    new ColorItemSizeStock(blueColorItem, "S(70CM)", 9),
                    new ColorItemSizeStock(blueColorItem, "M(80CM)", 9)
            ));
            colorItemImages.addAll(List.of(
                    new ColorItemImage(blueColorItem, new UploadFile("man-blue-pants.jpg", "man-blue-pants-uuid.jpg"), 1),
                    new ColorItemImage(blueColorItem, new UploadFile("man-blue-pants-detail.jpg", "man-blue-pants-detail-uuid.jpg"), 2),
                    new ColorItemImage(blueColorItem, new UploadFile("man-blue-pants-detail2.jpg", "man-blue-pants-detail2-uuid.jpg"), 3)
            ));

            // Color: Navy
            Color navy = colorRepository.findByName("네이비").get();
            ColorItem navyColorItem = new ColorItem(item, navy);
            colorItems.add(navyColorItem);
            colorItemSizeStocks.addAll(List.of(
                    new ColorItemSizeStock(navyColorItem, "S(70CM)", 0),
                    new ColorItemSizeStock(navyColorItem, "M(80CM)", 0)
            ));
            colorItemImages.addAll(List.of(
                    new ColorItemImage(navyColorItem, new UploadFile("man-navy-pants.jpg", "man-navy-pants-uuid.jpg"), 1),
                    new ColorItemImage(navyColorItem, new UploadFile("man-navy-pants-detail.jpg", "man-navy-pants-detail-uuid.jpg"), 2),
                    new ColorItemImage(navyColorItem, new UploadFile("man-navy-pants-detail2.jpg", "man-navy-pants-detail2-uuid.jpg"), 3)
            ));

            itemService.saveItem(item, colorItems, colorItemSizeStocks);
            itemService.saveColorItemImages(colorItemImages);

            colorItems.clear();
            colorItemSizeStocks.clear();
            colorItemImages.clear();
            //-------------------------------------------------------------------------------
            category = categoryRepository.findByName("니트/가디건").get();

            item = new Item(
                    55000,
                    55000,
                    Gender.FEMALE,
                    "25 S/S",
                    "여 워셔블 긴팔 가디건",
                    "원사에 실 꼬임을 많이 준 면 100% 강연 소재로 제작되어 탄탄하고 형태 안정성이 우수하여,\n" +
                            "기계 세탁이 가능하고 관리가 용이한 워셔블 긴팔 가디건입니다.",
                    LocalDate.parse("2024-07-01"),
                    "중국",
                    "(주)신세계인터내셔날",
                    category,
                    "면 100%(상표,장식,무늬,자수,밴드,심지,보강재 제외)",
                    "상품별 정확한 세탁방법은 세탁취급주의 라벨을 확인한 뒤 세탁 바랍니다."
            );
            item.setCreatedAt(LocalDateTime.parse("2025-08-01T00:00:00"));

            // Color: Blue
            Color black = colorRepository.findByName("블랙").get();
            ColorItem cardiganBlack = new ColorItem(item, black);
            colorItems.add(cardiganBlack);

            colorItemSizeStocks.addAll(List.of(
                    new ColorItemSizeStock(cardiganBlack, "S(70CM)", 10),
                    new ColorItemSizeStock(cardiganBlack, "M(80CM)", 10)
            ));
            colorItemImages.addAll(List.of(
                    new ColorItemImage(cardiganBlack, new UploadFile("woman-black-neat.jpg", "woman-black-neat-3da68c93-01da-4dd9-b61c-e58d260c8afc.jpg"), 1),
                    new ColorItemImage(cardiganBlack, new UploadFile("woman-black-neat-detail.jpg", "woman-black-neat-detail-0fe08476-a162-4187-b071-b080a774c46d.jpg"), 2)
            ));

            Color white = colorRepository.findByName("화이트").get();
            ColorItem cardiganWhite = new ColorItem(item, white);
            colorItems.add(cardiganWhite);
            colorItemSizeStocks.addAll(List.of(
                    new ColorItemSizeStock(cardiganWhite, "S(70CM)", 10),
                    new ColorItemSizeStock(cardiganWhite, "M(80CM)", 10)
            ));
            colorItemImages.addAll(List.of(
                    new ColorItemImage(cardiganWhite, new UploadFile("woman-white-neat.jpg", "woman-white-neat-7d26b6a1-d9f3-42a6-b501-85291e51e297.jpg"), 1),
                    new ColorItemImage(cardiganWhite, new UploadFile("woman-white-neat-detail.jpg", "woman-white-neat-detail-4b088136-7064-431a-8e59-eeae60f9ae5d.jpg"), 2)
            ));

            itemService.saveItem(item, colorItems, colorItemSizeStocks);
            itemService.saveColorItemImages(colorItemImages);
            colorItems.clear();
            colorItemSizeStocks.clear();
            colorItemImages.clear();
        }

        private void initManyItem(int ITEM_COUNT) {
            log.info("initManyItem 실행 중");
            Category category = categoryRepository.findByName("팬츠").get();

            List<Item> items = new ArrayList<>();

            for(int i=0; i<ITEM_COUNT; i++) {
                Item item = new Item(
                        49900+i,
                        39900+i,
                        Gender.FEMALE,
                        "24 F/W",
                        "여 코듀로이 와이드 팬츠"+i,
                        "무형광 원단입니다. 전 년 상품 자주히트와 동일한 소재이며, 네이밍이변경되었습니다.",
                        LocalDate.parse("2024-08-01").plusDays(i),
                        "방글라데시",
                        "(주)신세계인터내셔날",
                        category,
                        "폴리에스터 94%, 폴리우레탄 6% (상표,장식,무늬,자수,밴드,심지,보강재 제외)",
                        "세제는 중성세제를 사용하고 락스 등의 표백제는 사용을 금합니다. 세탁 시 삶아 빨 경우 섬유의 특성이 소멸되어 수축 및 물빠짐의 우려가 있으므로 미온 세탁하시기 바랍니다.");
                item.setCreatedAt(LocalDateTime.parse("2025-06-01T00:00:00"));
                items.add(item);
            }

            //itemRepository.saveAll(items);
            jdbcTemplateRepository.saveItems(items);
            initManyColorItem();
            initManyStockImage();
        }

        private void initManyColorItem() {
            log.info("initManyColorItem 실행 중");
            Color ivory = colorRepository.findByName("아이보리").get();
            Color pink = colorRepository.findByName("핑크").get();

            List<ColorItem> colorItems = new ArrayList<>();

            List<Item> items = itemRepository.findAll();
            for (Item item : items) {
                // Color: 아이보리
                ColorItem ivoryColorItem = new ColorItem(item, ivory);
                colorItems.add(ivoryColorItem);

                // Color: 핑크
                ColorItem pinkColorItem = new ColorItem(item, pink);
                colorItems.add(pinkColorItem);

            }
            jdbcTemplateRepository.saveColorItems(colorItems);
        }

        private void initManyStockImage() {
            log.info("initManyStockImage 실행 중");
            List<ColorItem> colorItems = colorItemRepository.findAllWithColor();

            List<ColorItemSizeStock> colorItemSizeStocks = new ArrayList<>();
            List<ColorItemImage> colorItemImages = new ArrayList<>();

            for (ColorItem colorItem : colorItems) {
                int i = 0;
                // 사이즈 재고 추가 (모든 색상 공통)
                colorItemSizeStocks.add(new ColorItemSizeStock(colorItem, "S(67CM)", 9));
                colorItemSizeStocks.add(new ColorItemSizeStock(colorItem, "M(67CM)", 9));

                //상품 이미지 s3에 이미 올려둔거 쓰는거라 이름 통일함
                // 색상별 이미지 분기
                switch (colorItem.getColor().getName()) {
                    case "아이보리" -> {
                        colorItemImages.add(new ColorItemImage(colorItem, new UploadFile("woman-ivory-pants.jpg", "woman-ivory-pants-uuid.jpg"), 1));
                        colorItemImages.add(new ColorItemImage(colorItem, new UploadFile("woman-ivory-pants-detail.jpg", "woman-ivory-pants-detail-uuid.jpg"), 2));
                    }
                    case "핑크" -> {
                        colorItemImages.add(new ColorItemImage(colorItem, new UploadFile("woman-pink-pants.jpg", "woman-pink-pants-uuid.jpg"), 1));
                        colorItemImages.add(new ColorItemImage(colorItem, new UploadFile("woman-pink-pants-detail.jpg", "woman-pink-pants-detail-uuid.jpg"), 2));
                    }
                    default -> {
                        // 다른 색상 처리 필요 시
                    }
                }
                ++i;
            }

            jdbcTemplateRepository.saveColorItemSizeStocks(colorItemSizeStocks);
            jdbcTemplateRepository.saveColorItemImages(colorItemImages);
        }

        private void initMember() {
            String password = bCryptPasswordEncoder.encode("test");

            Member admin = Member.builder().provider(Provider.LOCAL).authority(Authority.ADMIN).email("test@tama.com").phone("01022223333").password(password).nickname("박유빈").height(170).weight(60).gender(Gender.FEMALE).build();
            memberRepository.save(admin);

            Member OAUTH2_MEMBER = Member.builder().provider(Provider.GOOGLE).authority(Authority.MEMBER).email("kimapbel@gmail.com").phone("01011112222").password(password).nickname("김참정").height(160).weight(50).gender(Gender.MALE).build();
            memberRepository.save(OAUTH2_MEMBER);

            Member ORIGINAL_MEMBER = Member.builder().provider(Provider.LOCAL).authority(Authority.MEMBER).email("pyb0402@tama.com").phone("01022223333").password(password).nickname("박유빈").height(170).weight(60).gender(Gender.FEMALE).build();
            memberRepository.save(ORIGINAL_MEMBER);
        }

        private void initMemberAddress() {
            Member member2 = memberRepository.findById(2L).get();
            memberService.saveMemberAddress(member2.getId(), "우리집", member2.getNickname(), member2.getPhone(), "4756", "서울 성동구 마장로39나길 8 (마장동, (주)문일화학)", "연구소 1층");
            memberService.saveMemberAddress(member2.getId(), "회사", member2.getNickname(), member2.getPhone(), "26454", "강원특별자치도 원주시 행구로 287 (행구동, 건영아파트)", "1동 101호");

            Member member3 = memberRepository.findById(3L).get();
            memberService.saveMemberAddress(member3.getId(), "우리집", member3.getNickname(), member3.getPhone(), "23036", "인천 강화군 강화읍 관청리 89-1", "행복 빌라 101호");
            memberService.saveMemberAddress(member3.getId(), "회사", member3.getNickname(), member3.getPhone(), "14713", "경기 부천시 소사구 송내동 303-5", "대룡타워 201호");
        }

        private void initReview() {
            List<Member> members = memberRepository.findAll();
            Review r1 = reviewRepository.save(Review.builder().member(members.get(0))
                    .orderItem(orderItemRepository.findById(1L).get())
                    //.colorItemSizeStock(colorItemSizeStockRepository.findById(1L).get())
                    .rating(2)
                    .comment("S사이즈로 아주 약간 큰 편이지만 키에 거의 딱 맞는거 같아요. 땀듯해서 입기 좋습니다ㅎㅎ").build());
            reviewRepository.save(r1);
            reviewService.updateCreatedAt(r1.getId());

            reviewRepository.save(Review.builder().member(members.get(1))
                    .orderItem(orderItemRepository.findById(2L).get())
                    .rating(4)
                    .comment("맘에 들어요. 편하게 잘 입을것 같아요. 블랙 사고싶네요").build());
        }

        private void initManyReview() {
            log.info("initManyReview 실행 중");

            ArrayList<String> texts = new ArrayList<>(List.of(
                    "원단이 부드럽고 착용감이 정말 좋아요.",
                    "색상이 사진이랑 거의 똑같아서 만족합니다.",
                    "생각보다 얇아서 여름에 입기 딱이에요.",
                    "사이즈가 조금 작게 나온 것 같아요. 한 치수 크게 사세요.",
                    "디자인이 예쁘고 마감도 깔끔합니다.",
                    "빨아도 변형이 없어서 오래 입을 수 있을 것 같아요.",
                    "겨울에 입기에는 조금 얇아서 아쉬워요.",
                    "가격 대비 품질이 좋아서 추천합니다.",
                    "재질이 탄탄해서 모양이 잘 잡혀요.",
                    "배송이 빨랐고 포장도 깔끔했습니다."
            ));


            List<Review> reviews = new ArrayList<>();
            List<OrderItem> orderItems = orderItemRepository.findAllWithOrderWithMember();

            for (OrderItem orderItem : orderItems) {
                Review review = Review.builder().member(orderItem.getOrder().getMember())
                            .orderItem(orderItem)
                            .rating(2)
                            .comment(texts.get(0))
                            .build();
                review.setCreatedAt(LocalDateTime.parse("2024-08-01T00:00:00").plusDays(orderItem.getId()));
                review.setUpdatedAt(LocalDateTime.parse("2024-08-01T00:00:00").plusDays(orderItem.getId()));
                reviews.add(review);
            }

            jdbcTemplateRepository.saveReviews(reviews);
        }

        private void initOrder() {
            SaveMemberOrderRequest request = new SaveMemberOrderRequest(UUID.randomUUID().toString(), "장재일", "01012349876", "05763"
                    , "서울특별시 송파구 성내천로 306 (마천동, 송파구보훈회관)", "회관 옆 파랑 건물", "집앞에 놔주세요", List.of(
                    new SaveOrderItemRequest(1L, 2),
                    new SaveOrderItemRequest(2L, 2)
            ));
            createMemberOrder(1L, request);

            SaveMemberOrderRequest request2 = new SaveMemberOrderRequest(UUID.randomUUID().toString(), "장재일", "01012349876", "05763"
                    , "서울특별시 송파구 성내천로 306 (마천동, 송파구보훈회관)", "회관 옆 파랑 건물", "집앞에 놔주세요", List.of(
                    new SaveOrderItemRequest(3L, 2),
                    new SaveOrderItemRequest(4L, 1)
            ));
            createMemberOrder(1L, request2);


            /*
            SaveMemberOrderRequest request = new SaveMemberOrderRequest(UUID.randomUUID().toString(), "장재일", "01012349876", "05763"
                    , "서울특별시 송파구 성내천로 306 (마천동, 송파구보훈회관)", "회관 옆 파랑 건물", "집앞에 놔주세요", List.of(
                    new SaveOrderItemRequest(1L, 2),
                    new SaveOrderItemRequest(3L, 3)
            ));
            createMemberOrder(1L, request);

            SaveMemberOrderRequest request2 = new SaveMemberOrderRequest(UUID.randomUUID().toString(), "김성원", "01021347851", "57353"
                    , "전라남도 담양군 금성면 금성공단길 87 (금성면)", "금성 정육점", "가게 앞에 놔주세요", List.of(
                    new SaveOrderItemRequest(5L, 1),
                    new SaveOrderItemRequest(2L, 1)
            ));
            createMemberOrder(1L, request2);

            SaveGuestOrderRequest saveGuestOrderRequest = new SaveGuestOrderRequest(UUID.randomUUID().toString(), "김수현", "ksh@tama.com", "01013249512", "김성원", "01021347851", "57353"
                    , "전라남도 담양군 금성면 금성공단길 87 (금성면)", "금성 정육점", "가게 앞에 놔주세요", List.of(
                    new SaveOrderItemRequest(4L, 1),
                    new SaveOrderItemRequest(6L, 1)
            ));
            createGuestOrder(saveGuestOrderRequest);

             */
        }

        private void createMemberOrder(Long memberId, SaveMemberOrderRequest request) {
            Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("등록되지 않은 회원입니다."));
            Delivery delivery = new Delivery(request.getZipCode(), request.getStreetAddress(), request.getDetailAddress(), request.getDeliveryMessage(), request.getReceiverNickname(), request.getReceiverPhone());
            List<OrderItem> orderItems = new ArrayList<>();

            List<Long> colorItemSizeStockIds = request.getOrderItems().stream().map(SaveOrderItemRequest::getColorItemSizeStockId).toList();
            List<ColorItemSizeStock> colorItemSizeStocks = colorItemSizeStockRepository.findAllWithColorItemAndItemByIdIn(colorItemSizeStockIds);

            for (SaveOrderItemRequest saveOrderItemRequest : request.getOrderItems()) {
                Long colorItemSizeStockId = saveOrderItemRequest.getColorItemSizeStockId();
                //영속성 컨텍스트 재사용
                ColorItemSizeStock colorItemSizeStock = colorItemSizeStockRepository.findById(colorItemSizeStockId).orElseThrow(() -> new IllegalArgumentException(colorItemSizeStockId + "는 동록되지 않은 상품입니다"));

                //가격 변동 or 할인 쿠폰 고려
                Integer price = colorItemSizeStock.getColorItem().getItem().getOriginalPrice();
                Integer discountedPrice = colorItemSizeStock.getColorItem().getItem().getNowPrice();
                int orderPrice = discountedPrice != null ? discountedPrice : price;

                OrderItem orderItem = OrderItem.builder().colorItemSizeStock(colorItemSizeStock).orderPrice(orderPrice).count(saveOrderItemRequest.getOrderCount()).build();
                orderItems.add(orderItem);
            }

            Order order = Order.createMemberOrder(request.getPaymentId(), member, delivery, orderItems);

            //order 저장후 orderItem 저장해야함
            orderRepository.save(order);
            jdbcTemplateRepository.saveOrderItems(orderItems);
        }

        private void createGuestOrder(SaveGuestOrderRequest request) {
            Delivery delivery = new Delivery(request.getZipCode(), request.getStreetAddress(), request.getDetailAddress(), request.getDeliveryMessage(), request.getReceiverNickname(), request.getReceiverPhone());
            List<OrderItem> orderItems = new ArrayList<>();

            List<Long> colorItemSizeStockIds = request.getOrderItems().stream().map(SaveOrderItemRequest::getColorItemSizeStockId).toList();
            List<ColorItemSizeStock> colorItemSizeStocks = colorItemSizeStockRepository.findAllWithColorItemAndItemByIdIn(colorItemSizeStockIds);

            for (SaveOrderItemRequest saveOrderItemRequest : request.getOrderItems()) {
                Long itemId = saveOrderItemRequest.getColorItemSizeStockId();
                //영속성 컨텍스트 재사용
                ColorItemSizeStock colorItemSizeStock = colorItemSizeStockRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException(itemId + "는 동록되지 않은 상품입니다"));

                //가격 변동 or 할인 쿠폰 고려
                Integer price = colorItemSizeStock.getColorItem().getItem().getOriginalPrice();
                Integer discountedPrice = colorItemSizeStock.getColorItem().getItem().getNowPrice();
                int orderPrice = discountedPrice != null ? discountedPrice : price;

                OrderItem orderItem = OrderItem.builder().colorItemSizeStock(colorItemSizeStock).orderPrice(orderPrice).count(saveOrderItemRequest.getOrderCount()).build();
                orderItems.add(orderItem);
            }
            Guest guest = new Guest(request.getSenderNickname(), request.getSenderEmail());
            Order order = Order.createGuestOrder(request.getPaymentId(), guest, delivery, orderItems);
            //order 저장후 orderItem 저장해야함
            orderRepository.save(order);
            jdbcTemplateRepository.saveOrderItems(orderItems);
        }

        private void initManyOrder(int ORDER_COUNT) {
            log.info("initManyOrder 실행 중");
            List<ColorItemSizeStock> foundColorItemSizeStocks = colorItemSizeStockRepository.findAll();
            MemberAddress manAddress = memberAddressRepository.findById(1L).get();
            MemberAddress womanAddress = memberAddressRepository.findById(3L).get();
            List<Order> newOrders = new ArrayList<>();
            List<OrderItem> newOrderItems = new ArrayList<>();
            List<Delivery> deliveries = new ArrayList<>();

            Member member;
            Member man = memberRepository.findById(2L).get();
            Member woman = memberRepository.findById(3L).get();
            int count = 0;

            for (ColorItemSizeStock colorItemSizeStock : foundColorItemSizeStocks) {
                //반복문 안에서만 쓸 리스트
                List<OrderItem> orderItems = new ArrayList<>();
                Long id = colorItemSizeStock.getId();
                int orderCount = id < 10 ? 2 : 1;
                SaveMemberOrderRequest request;

                if(id % 2 == 0){
                    request = new SaveMemberOrderRequest(UUID.randomUUID().toString(), manAddress.getReceiverNickName(), manAddress.getReceiverPhone(), manAddress.getZipCode()
                            , manAddress.getStreet(), manAddress.getDetail(), "문 앞에 놔주세요", List.of(
                            new SaveOrderItemRequest(id, orderCount)
                    ));
                    member = man;
                }else {
                    request = new SaveMemberOrderRequest(UUID.randomUUID().toString(), womanAddress.getReceiverNickName(), womanAddress.getReceiverPhone(), womanAddress.getZipCode()
                            , womanAddress.getStreet(), womanAddress.getDetail(), "현관에 놔주세요", List.of(
                            new SaveOrderItemRequest(id, orderCount)
                    ));
                    member = woman;
                }

                Delivery delivery = new Delivery(request.getZipCode(), request.getStreetAddress(), request.getDetailAddress(), request.getDeliveryMessage(), request.getReceiverNickname(), request.getReceiverPhone());
                delivery.setCreatedAt(LocalDateTime.parse("2024-08-01T00:00:00").plusDays(id));
                delivery.setUpdatedAt(LocalDateTime.parse("2024-08-01T00:00:00").plusDays(id));
                deliveries.add(delivery);

                for (SaveOrderItemRequest saveOrderItemRequest : request.getOrderItems()) {
                    //가격 변동 or 할인 쿠폰 고려
                    Integer nowPrice = colorItemSizeStock.getColorItem().getItem().getNowPrice();
                    int orderPrice = nowPrice;

                    OrderItem orderItem = OrderItem.builder().colorItemSizeStock(colorItemSizeStock).orderPrice(orderPrice)
                            .count(saveOrderItemRequest.getOrderCount()).build();
                    newOrderItems.add(orderItem);
                    orderItems.add(orderItem);
                }

                Order order = Order.createMemberOrder(request.getPaymentId(), member, delivery, orderItems);
                order.setCreatedAt(LocalDateTime.parse("2024-08-01T00:00:00").plusDays(id));
                order.setUpdatedAt(LocalDateTime.parse("2024-08-01T00:00:00").plusDays(id));
                newOrders.add(order);

                orderItems.clear();
                if(++count == ORDER_COUNT) break;
            }

            jdbcTemplateRepository.saveDeliveries(deliveries);
            List<Delivery> foundDeliveries = deliveryRepository.findAll();
            Map<LocalDateTime, Delivery> deliveryMap = foundDeliveries.stream()
                    .collect(Collectors.toMap(
                            BaseEntity::getCreatedAt,
                            o -> o
                    ));

            for (Delivery delivery : deliveries) {
                Delivery foundDelivery = deliveryMap.get(delivery.getCreatedAt());
                if (foundDelivery != null) delivery.setIdByBatchId(foundDelivery.getId());
            }

            jdbcTemplateRepository.saveOrders(newOrders);

            //시간-PK 매핑
            List<Order> foundOrders = orderRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
            Map<LocalDateTime, Long> foundOrderIdMap = foundOrders.stream()
                    .collect(Collectors.toMap(
                            BaseEntity::getCreatedAt,
                            Order::getId
                    ));

            //1.맵에서 날짜로 PK를 꺼낸다
            //2.order 엔티티에 맞는 pk를 넣는다
            //newOrders는 pk가 존재하지 않는다. (배치로 저장했기 때문에)
            for (Order newOrder : newOrders) {
                Long orderId = foundOrderIdMap.get(newOrder.getCreatedAt());
                newOrder.setIdByBatchId(orderId);
            }

            jdbcTemplateRepository.saveOrderItems(newOrderItems);
        }

        public void initBestItemCache(){
            CustomPageRequest customPageRequest = new CustomPageRequest(1,10);

            //전체 인기 상품
            List<Long> emptyCategoryIds = new ArrayList<>();
            List<CategoryBestItemQueryResponse> allBestItems = itemQueryRepository.findCategoryBestItemWithPaging(emptyCategoryIds, customPageRequest);

            //아우터 인기 상품
            List<Long> outerCategoryIds = new ArrayList<>();
            Long outerCategoryId = 1L;
            Category outerCategory = categoryRepository.findWithChildrenById(outerCategoryId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 category를 찾을 수 없습니다"));
            outerCategoryIds.add(outerCategoryId);
            outerCategoryIds.addAll(outerCategory.getChildren().stream().map(Category::getId).toList());
            List<CategoryBestItemQueryResponse> outerBestItems = itemQueryRepository.findCategoryBestItemWithPaging(outerCategoryIds, customPageRequest);

            //상의 인기 상품
            List<Long> topCategoryIds = new ArrayList<>();
            Long topCategoryId = 5L;
            Category topCategory = categoryRepository.findWithChildrenById(topCategoryId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 category를 찾을 수 없습니다"));
            topCategoryIds.add(topCategoryId);
            topCategoryIds.addAll(topCategory.getChildren().stream().map(Category::getId).toList());
            List<CategoryBestItemQueryResponse> topBestItems = itemQueryRepository.findCategoryBestItemWithPaging(topCategoryIds, customPageRequest);

            //하의 인기 상품
            List<Long> bottomCategoryIds = new ArrayList<>();
            Long bottomCategoryId = 11L;
            Category bottomCategory = categoryRepository.findWithChildrenById(bottomCategoryId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 category를 찾을 수 없습니다"));
            bottomCategoryIds.add(bottomCategoryId);
            bottomCategoryIds.addAll(bottomCategory.getChildren().stream().map(Category::getId).toList());
            List<CategoryBestItemQueryResponse> bottomBestItems = itemQueryRepository.findCategoryBestItemWithPaging(bottomCategoryIds, customPageRequest);

            //캐시 저장
            cacheService.save(MyCacheType.BEST_ITEM.getName(), String.valueOf(BestItem.ALL_BEST_ITEM), allBestItems);
            cacheService.save(MyCacheType.BEST_ITEM.getName(), String.valueOf(BestItem.OUTER_BEST_ITEM), outerBestItems);
            cacheService.save(MyCacheType.BEST_ITEM.getName(), String.valueOf(BestItem.TOP_BEST_ITEM), topBestItems);
            cacheService.save(MyCacheType.BEST_ITEM.getName(), String.valueOf(BestItem.BOTTOM_BEST_ITEM), bottomBestItems);
        }

    }

}