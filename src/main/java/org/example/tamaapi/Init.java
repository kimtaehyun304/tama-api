package org.example.tamaapi;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.cache.BestItem;
import org.example.tamaapi.cache.MyCacheType;
import org.example.tamaapi.domain.*;
import org.example.tamaapi.domain.order.OrderStatus;
import org.example.tamaapi.domain.user.coupon.Coupon;
import org.example.tamaapi.domain.user.coupon.CouponType;
import org.example.tamaapi.domain.user.coupon.MemberCoupon;
import org.example.tamaapi.domain.item.*;
import org.example.tamaapi.domain.order.Delivery;
import org.example.tamaapi.domain.order.Order;
import org.example.tamaapi.domain.order.OrderItem;
import org.example.tamaapi.domain.user.*;
import org.example.tamaapi.dto.UploadFile;
import org.example.tamaapi.dto.requestDto.CustomPageRequest;

import org.example.tamaapi.dto.requestDto.order.OrderItemRequest;
import org.example.tamaapi.dto.requestDto.order.OrderRequest;
import org.example.tamaapi.exception.OrderFailException;
import org.example.tamaapi.repository.*;
import org.example.tamaapi.repository.item.*;
import org.example.tamaapi.repository.item.query.ItemQueryRepository;
import org.example.tamaapi.repository.item.query.dto.CategoryBestItemQueryResponse;
import org.example.tamaapi.repository.order.DeliveryRepository;
import org.example.tamaapi.repository.order.OrderItemRepository;
import org.example.tamaapi.repository.order.OrderRepository;
import org.example.tamaapi.service.*;
import org.example.tamaapi.util.ErrorMessageUtil;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.example.tamaapi.util.ErrorMessageUtil.NOT_FOUND_COUPON;

@Component
@RequiredArgsConstructor
@Slf4j
public class Init {

    private final InitService initService;
    private final Environment environment;

    @PostConstruct
    public void init() {
        String ddlAuto = environment.getProperty("spring.jpa.hibernate.ddl-auto");

        if (!ddlAuto.equals("none")) {
            initService.initCommon();
            initService.initBigData();
        }

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
        private final CouponRepository couponRepository;
        private final MemberCouponRepository memberCouponRepository;
        private final OrderService orderService;
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

        public boolean isNotInit() {
            return colorItemSizeStockRepository.count() == 0 &&
                    categoryRepository.count() == 0 &&
                    memberRepository.count() == 0 &&
                    colorRepository.count() == 0 &&
                    reviewRepository.count() == 0 &&
                    orderRepository.count() == 0 &&
                    orderItemRepository.count() == 0;
        }

        public void initCommon() {
            initCategory();
            initColor();
            initMember();
            initMemberAddress();
            initCoupon();
        }

        public void initSmallData() {
            initItem();
            initOrder();
            initReview();
        }

        public void initBigData() {
            initManyItem(100000);
            initManyOrder(30000);
            initManyReview();
        }

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
            colorItemSizeStocks.addAll(
                    List.of(new ColorItemSizeStock(ivoryColorItem, "S(67CM)", 100),
                            new ColorItemSizeStock(ivoryColorItem, "M(67CM)", 100)));
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
            colorItemSizeStocks.addAll(
                    List.of(new ColorItemSizeStock(pinkColorItem, "S(67CM)", 1000)
                            , new ColorItemSizeStock(pinkColorItem, "M(67CM)", 1000)));
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
                    new ColorItemSizeStock(blueColorItem, "S(70CM)", 1000),
                    new ColorItemSizeStock(blueColorItem, "M(80CM)", 1000)
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
                    new ColorItemSizeStock(navyColorItem, "S(70CM)", 1000),
                    new ColorItemSizeStock(navyColorItem, "M(80CM)", 1000)
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
                    new ColorItemSizeStock(cardiganBlack, "S(70CM)", 1000),
                    new ColorItemSizeStock(cardiganBlack, "M(80CM)", 1000)
            ));
            colorItemImages.addAll(List.of(
                    new ColorItemImage(cardiganBlack, new UploadFile("woman-black-neat.jpg", "woman-black-neat-3da68c93-01da-4dd9-b61c-e58d260c8afc.jpg"), 1),
                    new ColorItemImage(cardiganBlack, new UploadFile("woman-black-neat-detail.jpg", "woman-black-neat-detail-0fe08476-a162-4187-b071-b080a774c46d.jpg"), 2)
            ));

            Color white = colorRepository.findByName("화이트").get();
            ColorItem cardiganWhite = new ColorItem(item, white);
            colorItems.add(cardiganWhite);
            colorItemSizeStocks.addAll(List.of(
                    new ColorItemSizeStock(cardiganWhite, "S(70CM)", 1000),
                    new ColorItemSizeStock(cardiganWhite, "M(80CM)", 1000)
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

        private void initMember() {
            String password = bCryptPasswordEncoder.encode("test");

            Member admin = Member.builder().provider(Provider.LOCAL).authority(Authority.ADMIN).email("admin@tama.com")
                    .phone("01011111111").password(password).nickname("박유빈")
                    .height(170).weight(60).gender(Gender.FEMALE).build();
            memberRepository.save(admin);

            Member OAUTH2_MEMBER = Member.builder().provider(Provider.GOOGLE).authority(Authority.MEMBER)
                    .email("kimapbel@gmail.com").phone("01011111112").password(password).nickname("김참정")
                    .height(160).weight(50).gender(Gender.MALE).point(1000000).build();
            memberRepository.save(OAUTH2_MEMBER);

            //체험 계정으로 제공
            Member ORIGINAL_MEMBER = Member.builder().provider(Provider.LOCAL).authority(Authority.MEMBER)
                    .email("burnaby033@naver.com").phone("01011111113").password(password).nickname("박유빈")
                    .height(170).weight(60).gender(Gender.FEMALE).point(1000000).build();
            memberRepository.save(ORIGINAL_MEMBER);
        }

        private void initMemberAddress() {
            List<Member> members = memberRepository.findAllByAuthority(Authority.MEMBER);

            memberService.saveMemberAddress(members.get(0).getId(), "우리집", members.get(0).getNickname(), members.get(0).getPhone(), "4756", "서울 성동구 마장로39나길 8 (마장동, (주)문일화학)", "연구소 1층");
            memberService.saveMemberAddress(members.get(0).getId(), "회사", members.get(0).getNickname(), members.get(0).getPhone(), "26454", "강원특별자치도 원주시 행구로 287 (행구동, 건영아파트)", "1동 101호");

            memberService.saveMemberAddress(members.get(1).getId(), "우리집", members.get(1).getNickname(), members.get(1).getPhone(), "23036", "인천 강화군 강화읍 관청리 89-1", "행복 빌라 101호");
            memberService.saveMemberAddress(members.get(1).getId(), "회사", members.get(1).getNickname(), members.get(1).getPhone(), "14713", "경기 부천시 소사구 송내동 303-5", "대룡타워 201호");
        }

        private void initCoupon() {
                Coupon percentCoupon1 = new Coupon(CouponType.PERCENT_DISCOUNT, 10, LocalDate.now().plusYears(1));
            Coupon percentCoupon2 = new Coupon(CouponType.PERCENT_DISCOUNT, 20, LocalDate.now().plusYears(1));
            Coupon percentCoupon3 = new Coupon(CouponType.PERCENT_DISCOUNT, 30, LocalDate.now().plusYears(1));

            Coupon fixedCoupon1 = new Coupon(CouponType.FIXED_DISCOUNT, 5000, LocalDate.now().plusYears(1));
            Coupon fixedCoupon2 = new Coupon(CouponType.FIXED_DISCOUNT, 10000, LocalDate.now().plusYears(1));
            Coupon fixedCoupon3 = new Coupon(CouponType.FIXED_DISCOUNT, 15000, LocalDate.now().plusYears(1));

            Coupon expiredCoupon = new Coupon(CouponType.FIXED_DISCOUNT, 10000, LocalDate.now().minusDays(7));
            Coupon usedCoupon = new Coupon(CouponType.FIXED_DISCOUNT, 10000, LocalDate.now().plusMonths(1));

            couponRepository.save(percentCoupon1);
            couponRepository.save(percentCoupon2);
            couponRepository.save(percentCoupon3);

            couponRepository.save(fixedCoupon1);
            couponRepository.save(fixedCoupon2);
            couponRepository.save(fixedCoupon3);

            couponRepository.save(expiredCoupon);
            couponRepository.save(usedCoupon);
            //----------------------------------------------------------------
            List<Member> members = memberRepository.findAllByAuthority(Authority.MEMBER);

            memberCouponRepository.save(new MemberCoupon(percentCoupon1, members.get(0), false));
            memberCouponRepository.save(new MemberCoupon(percentCoupon2, members.get(0), false));
            memberCouponRepository.save(new MemberCoupon(percentCoupon3, members.get(0), false));

            memberCouponRepository.save(new MemberCoupon(fixedCoupon1, members.get(0), false));
            memberCouponRepository.save(new MemberCoupon(fixedCoupon2, members.get(0), false));
            memberCouponRepository.save(new MemberCoupon(fixedCoupon3, members.get(0), false));

            memberCouponRepository.save(new MemberCoupon(expiredCoupon, members.get(0), false));
            memberCouponRepository.save(new MemberCoupon(usedCoupon, members.get(0), true));

            //----------------------------------------------------------------
            memberCouponRepository.save(new MemberCoupon(percentCoupon1, members.get(1), false));
            memberCouponRepository.save(new MemberCoupon(percentCoupon2, members.get(1), false));
            memberCouponRepository.save(new MemberCoupon(percentCoupon3, members.get(1), false));

            memberCouponRepository.save(new MemberCoupon(fixedCoupon1, members.get(1), false));
            memberCouponRepository.save(new MemberCoupon(fixedCoupon2, members.get(1), false));
            memberCouponRepository.save(new MemberCoupon(fixedCoupon3, members.get(1), false));

            memberCouponRepository.save(new MemberCoupon(expiredCoupon, members.get(1), false));
            memberCouponRepository.save(new MemberCoupon(usedCoupon, members.get(1), true));
        }

        private void initOrder() {
            Member member1 = memberRepository.findAllByAuthority(Authority.MEMBER).get(0);
            MemberAddress member1DefaultAddress = memberAddressRepository.findByMemberIdAndIsDefault(member1.getId(), true).get();
            List<MemberCoupon> member1Coupons = memberCouponRepository.findNotExpiredAndUnusedCouponsByMemberId(member1.getId());


            createMemberOrder(member1.getId(), new OrderRequest(UUID.randomUUID().toString(), member1.getNickname(), member1.getEmail(), member1.getNickname()
                    , member1.getPhone(), member1DefaultAddress.getZipCode(), member1DefaultAddress.getStreet(), member1DefaultAddress.getDetail(), "현관문 앞에 놔주세요",
                    null, 0, List.of(
                    new OrderItemRequest(1L, 2),
                    new OrderItemRequest(2L, 1)
            )));

            MemberAddress member1Address = memberAddressRepository.findByMemberIdAndIsDefault(member1.getId(), false).get();
            createMemberOrder(member1.getId(), new OrderRequest(UUID.randomUUID().toString(), member1.getNickname(), member1.getEmail(), member1.getNickname()
                    , member1.getPhone(), member1Address.getZipCode(), member1Address.getStreet(), member1Address.getDetail(), "회사 복도에 놔주세요",
                    member1Coupons.get(0).getId(), 5000, List.of(
                    new OrderItemRequest(1L, 3),
                    new OrderItemRequest(2L, 1)
            )));

            //-------------------------------------
            Member member2 = memberRepository.findAllByAuthority(Authority.MEMBER).get(1);
            MemberAddress member2DefaultAddress = memberAddressRepository.findByMemberIdAndIsDefault(member2.getId(), true).get();
            List<MemberCoupon> member2Coupons = memberCouponRepository.findNotExpiredAndUnusedCouponsByMemberId(member2.getId());

            createMemberOrder(member2.getId(),
                    new OrderRequest(UUID.randomUUID().toString(), member2.getNickname(), member2.getEmail(), member2.getNickname()
                            , member2.getPhone(), member2DefaultAddress.getZipCode(), member2DefaultAddress.getStreet(), member2DefaultAddress.getDetail(), "현관문 앞에 놔주세요",
                            null, 0, List.of(
                            new OrderItemRequest(1L, 2),
                            new OrderItemRequest(2L, 2))));

            MemberAddress member2Address = memberAddressRepository.findByMemberIdAndIsDefault(member2.getId(), false).get();
            createMemberOrder(member2.getId(), new OrderRequest(UUID.randomUUID().toString(), member2.getNickname(), member2.getEmail(), member2.getNickname()
                    , member2.getPhone(), member2Address.getZipCode(), member2Address.getStreet(), member2Address.getDetail(), "현관문 앞에 놔주세요",
                    member2Coupons.get(0).getId(), 5000, List.of(
                    new OrderItemRequest(1L, 2),
                    new OrderItemRequest(2L, 2)
            )));
            //-------------------------------------
            String guestName = "장재일";
            OrderRequest guestRequest = new OrderRequest(UUID.randomUUID().toString(), guestName, "burnaby033@naver.com", guestName, "010111122222", "05763"
                    , "서울특별시 송파구 성내천로 306 (마천동, 송파구보훈회관)", "회관 옆 파랑 건물", "집앞에 놔주세요",
                    null, 0, List.of(
                    new OrderItemRequest(3L, 2),
                    new OrderItemRequest(4L, 1)
            ));
            createGuestOrder(guestRequest);


            OrderRequest guestRequest2 = new OrderRequest(UUID.randomUUID().toString(), guestName, "burnaby033@naver.com", guestName, "010111122222", "05763"
                    , "서울특별시 송파구 성내천로 306 (마천동, 송파구보훈회관)", "회관 옆 파랑 건물", "집앞에 놔주세요",
                    null, 0, List.of(
                    new OrderItemRequest(5L, 2),
                    new OrderItemRequest(6L, 1)
            ));
            createGuestOrder(guestRequest2);


        }

        private void initReview() {
            List<Member> members = memberRepository.findAll();
            Review r1 = reviewRepository.save(Review.builder().member(members.get(0))
                    .orderItem(orderItemRepository.findById(1L).get())
                    .rating(2)
                    .comment("S사이즈로 아주 약간 큰 편이지만 키에 거의 딱 맞는거 같아요. 땀듯해서 입기 좋습니다ㅎㅎ").build());
            reviewRepository.save(r1);
            reviewService.updateCreatedAt(r1.getId());

            reviewRepository.save(Review.builder().member(members.get(1))
                    .orderItem(orderItemRepository.findById(2L).get())
                    .rating(4)
                    .comment("맘에 들어요. 편하게 잘 입을것 같아요. 블랙 사고싶네요").build());
        }


        private void createMemberOrder(Long memberId, OrderRequest request) {
            Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("등록되지 않은 회원입니다."));
            Delivery delivery = new Delivery(request.getZipCode(), request.getStreetAddress(), request.getDetailAddress(), request.getDeliveryMessage(), request.getReceiverNickname(), request.getReceiverPhone());
            List<OrderItem> orderItems = new ArrayList<>();

            List<Long> colorItemSizeStockIds = request.getOrderItems().stream().map(OrderItemRequest::getColorItemSizeStockId).toList();
            List<ColorItemSizeStock> colorItemSizeStocks = colorItemSizeStockRepository.findAllWithColorItemAndItemByIdIn(colorItemSizeStockIds);

            for (OrderItemRequest orderItemRequest : request.getOrderItems()) {
                Long colorItemSizeStockId = orderItemRequest.getColorItemSizeStockId();
                //영속성 컨텍스트 재사용
                ColorItemSizeStock colorItemSizeStock = colorItemSizeStockRepository.findById(colorItemSizeStockId).orElseThrow(() -> new IllegalArgumentException(colorItemSizeStockId + "는 동록되지 않은 상품입니다"));

                //가격 변동 or 할인 쿠폰 고려
                Integer price = colorItemSizeStock.getColorItem().getItem().getOriginalPrice();
                Integer discountedPrice = colorItemSizeStock.getColorItem().getItem().getNowPrice();
                int orderPrice = discountedPrice != null ? discountedPrice : price;

                OrderItem orderItem = OrderItem.builder().colorItemSizeStock(colorItemSizeStock).orderPrice(orderPrice).count(orderItemRequest.getOrderCount()).build();
                orderItems.add(orderItem);
            }

            MemberCoupon memberCoupon = null;

            Long memberCouponId = request.getMemberCouponId();
            //주문 후에, 쿠폰 처리하는게 이상적이자만, saveOrder에 memberCoupon 넘겨야해서 미리 처리함
            //주문 예외나면 쿠폰 롤백되서 미리 처리해도 괜찮음
            if (memberCouponId != null) {
                memberCoupon = memberCouponRepository.findById(memberCouponId)
                        .orElseThrow(() -> new OrderFailException(NOT_FOUND_COUPON));
                memberCoupon.changeIsUsed(true);
            }
            //사용한 포인트 차감
            member.minusPoint(request.getUsedPoint());

            int orderItemsPrice = orderService.getOrderItemsPrice(request.getOrderItems());
            int usedCouponPrice = orderService.getCouponPrice(memberCoupon, orderItemsPrice);
            int shippingFee = orderService.getShippingFee(orderItemsPrice);
            Order order = Order.createMemberOrder(request.getPaymentId(), member, delivery, null, usedCouponPrice, request.getUsedPoint(), shippingFee, orderItems);
            order.changeStatus(OrderStatus.COMPLETED);
            //order 저장후 orderItem 저장해야함
            orderRepository.save(order);
            jdbcTemplateRepository.saveOrderItems(orderItems);
        }

        private void createGuestOrder(OrderRequest request) {
            Delivery delivery = new Delivery(request.getZipCode(), request.getStreetAddress(), request.getDetailAddress(), request.getDeliveryMessage(), request.getReceiverNickname(), request.getReceiverPhone());
            List<OrderItem> orderItems = new ArrayList<>();

            List<Long> colorItemSizeStockIds = request.getOrderItems().stream().map(OrderItemRequest::getColorItemSizeStockId).toList();
            List<ColorItemSizeStock> colorItemSizeStocks = colorItemSizeStockRepository.findAllWithColorItemAndItemByIdIn(colorItemSizeStockIds);

            for (OrderItemRequest orderItemRequest : request.getOrderItems()) {
                Long itemId = orderItemRequest.getColorItemSizeStockId();
                //영속성 컨텍스트 재사용
                ColorItemSizeStock colorItemSizeStock = colorItemSizeStockRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException(itemId + "는 동록되지 않은 상품입니다"));

                //가격 변동 or 할인 쿠폰 고려
                Integer price = colorItemSizeStock.getColorItem().getItem().getOriginalPrice();
                Integer discountedPrice = colorItemSizeStock.getColorItem().getItem().getNowPrice();
                int orderPrice = discountedPrice != null ? discountedPrice : price;

                OrderItem orderItem = OrderItem.builder().colorItemSizeStock(colorItemSizeStock).orderPrice(orderPrice).count(orderItemRequest.getOrderCount()).build();
                orderItems.add(orderItem);
            }
            Guest guest = new Guest(request.getSenderNickname(), request.getSenderEmail());
            int orderItemsPrice = orderService.getOrderItemsPrice(request.getOrderItems());
            int shippingFee = orderService.getShippingFee(orderItemsPrice);
            Order order = Order.createGuestOrder(request.getPaymentId(), guest, delivery, shippingFee, orderItems);

            //자동 구매롹정 테스트를 위해 배송완료 처리
            //사용자 주문한 날짜
            order.setCreatedAt(LocalDateTime.now().minusDays(9));
            //배송이 완료되서 변경된 날짜
            order.setUpdatedAt(LocalDateTime.now().minusDays(7));
            order.changeStatus(OrderStatus.DELIVERED);

            //order 저장후 orderItem 저장해야함
            orderRepository.save(order);
            jdbcTemplateRepository.saveOrderItems(orderItems);
        }

        private void initManyItem(int ITEM_COUNT) {
            log.info("initManyItem 실행 중");
            Category category = categoryRepository.findByName("팬츠").get();

            List<Item> items = new ArrayList<>();

            LocalDate dateOfManufacture = LocalDate.parse("2010-01-01");
            LocalDateTime createdDate = LocalDateTime.parse("2011-01-01T00:00:00");
            for (int i = 0; i < ITEM_COUNT; i++) {
                createdDate = createdDate.plusHours(1);
                Item item = new Item(
                        49900 + i,
                        39900 + i,
                        Gender.FEMALE,
                        "24 F/W",
                        "여 코듀로이 와이드 팬츠" + i,
                        "무형광 원단입니다. 전 년 상품 자주히트와 동일한 소재이며, 네이밍이변경되었습니다.",
                        dateOfManufacture,
                        "방글라데시",
                        "(주)신세계인터내셔날",
                        category,
                        "폴리에스터 94%, 폴리우레탄 6% (상표,장식,무늬,자수,밴드,심지,보강재 제외)",
                        "세제는 중성세제를 사용하고 락스 등의 표백제는 사용을 금합니다. 세탁 시 삶아 빨 경우 섬유의 특성이 소멸되어 수축 및 물빠짐의 우려가 있으므로 미온 세탁하시기 바랍니다.");
                item.setCreatedAt(createdDate);
                item.setUpdatedAt(createdDate);
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
                colorItemSizeStocks.add(new ColorItemSizeStock(colorItem, "S(67CM)", 1000));
                colorItemSizeStocks.add(new ColorItemSizeStock(colorItem, "M(67CM)", 1000));

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

        private void initManyOrder(int ORDER_COUNT) {
            log.info("initManyOrder 실행 중");
            List<ColorItemSizeStock> foundColorItemSizeStocks = colorItemSizeStockRepository.findAll();

            List<Member> members = memberRepository.findAllByAuthority(Authority.MEMBER);
            Member member0 = members.get(0);
            Member member1 = members.get(1);

            MemberAddress member0Address = memberAddressRepository.findByMemberIdAndIsDefault(member0.getId(), true).get();
            MemberAddress member1Address = memberAddressRepository.findByMemberIdAndIsDefault(member1.getId(), true).get();
            List<Order> newOrders = new ArrayList<>();
            List<OrderItem> newOrderItems = new ArrayList<>();
            List<Delivery> deliveries = new ArrayList<>();

            Member member;
            MemberAddress memberAddress;

            int count = 0;

            LocalDateTime createdDate = LocalDateTime.parse("2020-01-01T00:00:00");
            for (ColorItemSizeStock colorItemSizeStock : foundColorItemSizeStocks) {
                //반복문 안에서만 쓸 리스트
                List<OrderItem> orderItems = new ArrayList<>();
                Long id = colorItemSizeStock.getId();
                int orderCount = id < 10 ? 2 : 1;
                OrderRequest request;

                if (id % 2 == 0) {
                    member = member0;
                    memberAddress = member0Address;
                } else {
                    member = member1;
                    memberAddress = member1Address;
                }

                request = new OrderRequest(UUID.randomUUID().toString(), null, null,
                        memberAddress.getReceiverNickName(), memberAddress.getReceiverPhone(), memberAddress.getZipCode()
                        , memberAddress.getStreet(), memberAddress.getDetail(), "문 앞에 놔주세요", null, 0,
                        List.of(
                                new OrderItemRequest(id, orderCount)
                        ));

                //order랑 delivery를 OrderRequest에서 한번에 받으므로, OrderRequest에서 delivery 정보 가져옴
                Delivery delivery = new Delivery(request.getZipCode(), request.getStreetAddress(), request.getDetailAddress(), request.getDeliveryMessage()
                        , request.getReceiverNickname(), request.getReceiverPhone());

                createdDate = createdDate.plusHours(1);
                delivery.setCreatedAt(createdDate);
                delivery.setUpdatedAt(createdDate);
                deliveries.add(delivery);

                for (OrderItemRequest orderItemRequest : request.getOrderItems()) {
                    //가격 변동 or 할인 쿠폰 고려
                    Integer nowPrice = colorItemSizeStock.getColorItem().getItem().getNowPrice();
                    int orderPrice = nowPrice;

                    OrderItem orderItem = OrderItem.builder().colorItemSizeStock(colorItemSizeStock).orderPrice(orderPrice)
                            .count(orderItemRequest.getOrderCount()).build();
                    newOrderItems.add(orderItem);
                    orderItems.add(orderItem);
                }
                //selcet 여러번 실행되서 오래걸리므로 shippingFee 0으로 지정
                //int orderItemsPrice = orderService.getOrderItemsPrice(request.getOrderItems());
                //int shippingFee = orderService.getShippingFee(orderItemsPrice);
                int shippingFee = 0;
                Order order = Order.createMemberOrder(request.getPaymentId(), member, delivery, null, 0, 0, shippingFee, orderItems);
                order.setCreatedAt(createdDate);
                order.setUpdatedAt(createdDate);
                newOrders.add(order);

                orderItems.clear();
                if (++count == ORDER_COUNT) break;
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

            LocalDateTime createdDate = LocalDateTime.parse("2020-01-01T00:00:00");
            for (OrderItem orderItem : orderItems) {
                Review review = Review.builder().member(orderItem.getOrder().getMember())
                        .orderItem(orderItem)
                        .rating(2)
                        .comment(texts.get(0))
                        .build();

                createdDate = createdDate.plusHours(1);
                review.setCreatedAt(createdDate);
                review.setUpdatedAt(createdDate);
                reviews.add(review);
            }

            jdbcTemplateRepository.saveReviews(reviews);
        }


        public void initBestItemCache() {
            CustomPageRequest customPageRequest = new CustomPageRequest(1, 10);

            for (BestItem bestItem : BestItem.values()) {
                List<Long> categoryIds = new ArrayList<>();

                if (bestItem.getCategoryId() != null) {
                    Category category = categoryRepository.findWithChildrenById(bestItem.getCategoryId())
                            .orElseThrow(() -> new IllegalArgumentException(ErrorMessageUtil.NOT_FOUND_CATEGORY));
                    categoryIds.add(bestItem.getCategoryId());
                    categoryIds.addAll(category.getChildren().stream().map(Category::getId).toList());
                }

                // 캐시 저장
                List<CategoryBestItemQueryResponse> bestItems = itemQueryRepository.findCategoryBestItemWithPaging(categoryIds, customPageRequest);
                cacheService.save(MyCacheType.BEST_ITEM, bestItem.name(), bestItems);
            }
        }

    }

}