package org.example.tamaapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.tamaapi.domain.item.Review;
import org.example.tamaapi.domain.user.Authority;
import org.example.tamaapi.domain.user.Member;
import org.example.tamaapi.dto.requestDto.item.save.SaveReviewRequest;
import org.example.tamaapi.dto.requestDto.order.OrderItemRequest;
import org.example.tamaapi.auth.jwt.TokenProvider;
import org.example.tamaapi.repository.MemberRepository;
import org.example.tamaapi.repository.item.ReviewRepository;
import org.example.tamaapi.repository.order.OrderItemRepository;
import org.example.tamaapi.service.OrderService;
import org.example.tamaapi.util.ErrorMessageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
// 테스트 끝나면 롤백 (auto_increment는 롤백 안됨)
@Transactional
class ReviewApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private OrderService orderService;
    private final String PAYMENT_ID = UUID.randomUUID().toString();
    @BeforeEach
    void saveOrder() throws Exception {
        Member member = memberRepository.findAllByAuthority(Authority.MEMBER).get(0);
        String receiverNickname = member.getNickname();
        String receiverPhone = member.getPhone();
        String zipCode = "25435";
        String streetAddress = "강원특별자치도 강릉시 사천면 중앙동로 71 (판교리, 판교리마을회관)";
        String detailAddress = "마을회관";
        String deliveryMessage = "문 앞에 놔주세요";

        List<OrderItemRequest> orderItems = new ArrayList<>(
                List.of(
                        new OrderItemRequest(10L, 1),
                        new OrderItemRequest(10L, 1)
                )
        );

        orderService.saveMemberOrder(
                PAYMENT_ID,
                member.getId(),
                receiverNickname,
                receiverPhone,
                zipCode,
                streetAddress,
                detailAddress,
                deliveryMessage,
                null,
                0,
                orderItems
        );
    }

    @Test
    void saveReview() throws  Exception {
        //given
        Long orderItemId = orderItemRepository.findAllWithOrderByPaymentId(PAYMENT_ID).get(0).getId();
        SaveReviewRequest request = new SaveReviewRequest(orderItemId, 5, "너무 이뻐이뻐요", 160, 50);
        Member member = orderItemRepository.findWithOrderById(orderItemId)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessageUtil.NOT_FOUND_ORDER))
                .getOrder().getMember();
        String accessToken = tokenProvider.generateToken(member);

        // when
        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("저장 완료"));

        //then
        Review review = reviewRepository.findByOrderItemId(orderItemId)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessageUtil.NOT_FOUND_REVIEW));
        assertThat(review.getOrderItem().getId()).isEqualTo(request.getOrderItemId());
        assertThat(review.getRating()).isEqualTo(request.getRating());
        assertThat(review.getComment()).isEqualTo(request.getComment());
        assertThat(review.getHeight()).isEqualTo(request.getHeight());
        assertThat(review.getWeight()).isEqualTo(request.getWeight());
    }
}