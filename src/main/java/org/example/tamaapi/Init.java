package org.example.tamaapi;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.domain.*;
import org.example.tamaapi.domain.item.*;
import org.example.tamaapi.dto.requestDto.order.SaveMemberOrderRequest;
import org.example.tamaapi.dto.requestDto.order.SaveOrderItemRequest;
import org.example.tamaapi.repository.*;
import org.example.tamaapi.repository.item.*;
import org.example.tamaapi.repository.order.OrderRepository;
import org.example.tamaapi.service.MemberService;
import org.example.tamaapi.service.OrderService;
import org.example.tamaapi.service.ReviewService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
        initService.initReview();
        initService.initOrder();
        initService.initMemberAddress();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {

        private final ItemRepository itemRepository;
        private final ColorItemRepository colorItemRepository;
        private final ColorItemSizeStockRepository colorItemSizeStockRepository;
        private final CategoryRepository categoryRepository;
        private final ItemImageRepository itemImageRepository;
        private final MemberRepository memberRepository;
        private final BCryptPasswordEncoder bCryptPasswordEncoder;
        private final ColorRepository colorRepository;
        private final ReviewRepository reviewRepository;
        private final ReviewService reviewService;
        private final OrderService orderService;
        private final OrderRepository orderRepository;
        private final JdbcTemplateRepository jdbcTemplateRepository;
        private final MemberService memberService;

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
                                    new SizeStockRequest("S(70CM)", 1),
                                    new SizeStockRequest("M(80CM)", 1)
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

        public void initMember() {
            //test@tama.com
            String password = bCryptPasswordEncoder.encode("test");
            Member member = Member.builder().provider(Provider.LOCAL).email("kimapbel@gmail.com").phone("01011112222").password(password).nickname("김참정").height(160).weight(50).gender(Gender.MALE).build();
            memberRepository.save(member);

            Member member2 = Member.builder().provider(Provider.GOOGLE).email("test@tama.com").phone("01022223333").password(password).nickname("박유빈").height(170).weight(60).gender(Gender.FEMALE).build();
            memberRepository.save(member2);
        }

        public void initMemberAddress() {
            Member member = memberRepository.findById(2L).get();
            memberService.saveMemberAddress(member.getId(), "우리집", member.getNickname(), member.getPhone(),"4756", "서울 성동구 마장로39나길 8 (마장동, (주)문일화학", "연구소 1층");
            memberService.saveMemberAddress(member.getId(), "회사", member.getNickname(), member.getPhone(),"26454", "강원특별자치도 원주시 행구로 287 (행구동, 건영아파트)", "1동 101호");
        }

        public void initReview() {
            List<Member> members = memberRepository.findAll();
            Review r1 = reviewRepository.save(Review.builder().member(members.get(0))
                    .colorItemSizeStock(colorItemSizeStockRepository.findById(1L).get())
                    .rating(2)
                    .comment("S사이즈로 아주 약간 큰 편이지만 키에 거의 딱 맞는거 같아요. 땀듯해서 입기 좋습니다ㅎㅎ").build());
            reviewRepository.save(r1);
            reviewService.updateCreatedAt(r1.getId());

            reviewRepository.save(Review.builder().member(members.get(1))
                    .colorItemSizeStock(colorItemSizeStockRepository.findById(2L).get())
                    .rating(4)
                    .comment("맘에 들어요. 편하게 잘 입을것 같아요. 블랙 사고싶네요").build());
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
                    ColorItemSizeStock colorItemSizeStock = ColorItemSizeStock.builder()
                            .colorItem(colorItem)
                            .size(sizeStock.getSize())
                            .stock(sizeStock.getStock())
                            .build();
                    colorItemSizeStockRepository.save(colorItemSizeStock);
                }
            }
        }

        public void initOrder() {

            SaveMemberOrderRequest request = new SaveMemberOrderRequest(UUID.randomUUID().toString(), "장재일", "01012349876", "05763"
                    , "서울특별시 송파구 성내천로 306 (마천동, 송파구보훈회관)", "회관 옆 파랑 건물", "집앞에 놔주세요", List.of(
                    new SaveOrderItemRequest(1L, 1),
                    new SaveOrderItemRequest(3L, 1)
            ));
            createOrder(2L, request);

            SaveMemberOrderRequest request2 = new SaveMemberOrderRequest(UUID.randomUUID().toString(), "김성원", "01021347851", "57353"
                    , "전라남도 담양군 금성면 금성공단길 87 (금성면)", "금성 정육점", "가게 앞에 놔주세요", List.of(
                    new SaveOrderItemRequest(5L, 1),
                    new SaveOrderItemRequest(2L, 1)
            ));
            createOrder(2L, request2);
        }

        public void createOrder(Long memberId, SaveMemberOrderRequest request) {
            Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("등록되지 않은 회원입니다."));
            Delivery delivery = new Delivery(request.getZipCode(), request.getStreetAddress(), request.getDetailAddress(), request.getDeliveryMessage(), request.getReceiverNickname(), request.getReceiverPhone());
            List<OrderItem> orderItems = new ArrayList<>();


            List<Long> colorItemSizeStockIds = request.getOrderItems().stream().map(SaveOrderItemRequest::getColorItemSizeStockId).toList();
            List<ColorItemSizeStock> colorItemSizeStocks = colorItemSizeStockRepository.findAllWithColorItemAndItemByIdIn(colorItemSizeStockIds);

            for (SaveOrderItemRequest saveOrderItemRequest : request.getOrderItems()) {
                Long itemId = saveOrderItemRequest.getColorItemSizeStockId();
                //영속성 컨텍스트 재사용
                ColorItemSizeStock colorItemSizeStock = colorItemSizeStockRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException(itemId + "는 동록되지 않은 상품입니다"));

                //가격 변동 or 할인 쿠폰 고려
                Integer price = colorItemSizeStock.getColorItem().getItem().getPrice();
                Integer discountedPrice = colorItemSizeStock.getColorItem().getItem().getDiscountedPrice();
                int orderPrice = discountedPrice != null ? discountedPrice : price;

                OrderItem orderItem = OrderItem.builder().colorItemSizeStock(colorItemSizeStock).orderPrice(orderPrice).count(saveOrderItemRequest.getOrderCount()).build();
                orderItems.add(orderItem);
            }

            Order order = Order.createMemberOrder(request.getPaymentId(), member, delivery, orderItems);
            //order 저장후 orderItem 저장해야함
            orderRepository.save(order);
            jdbcTemplateRepository.saveOrderItems(orderItems);
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

        public Color getColor() {
            return color;
        }

        public String getImageSrc() {
            return imageSrc;
        }

        public List<String> getImageDetails() {
            return imageDetails;
        }

        public List<SizeStockRequest> getSizeStocks() {
            return sizeStocks;
        }
    }

    static class SizeStockRequest {
        private String size;
        private int stock;

        // 생성자 및 getter/setter
        public SizeStockRequest(String size, int stock) {
            this.size = size;
            this.stock = stock;
        }

        public String getSize() {
            return size;
        }

        public int getStock() {
            return stock;
        }
    }


}