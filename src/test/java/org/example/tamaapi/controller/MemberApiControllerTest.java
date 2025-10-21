package org.example.tamaapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.tamaapi.cache.MyCacheType;
import org.example.tamaapi.domain.user.Authority;
import org.example.tamaapi.domain.user.Member;
import org.example.tamaapi.domain.user.MemberAddress;
import org.example.tamaapi.domain.user.Provider;
import org.example.tamaapi.dto.requestDto.LoginRequest;
import org.example.tamaapi.dto.requestDto.member.SaveMemberAddressRequest;
import org.example.tamaapi.dto.requestDto.member.SignUpMemberRequest;
import org.example.tamaapi.dto.requestDto.member.UpdateMemberDefaultAddressRequest;
import org.example.tamaapi.dto.requestDto.member.UpdateMemberInformationRequest;
import org.example.tamaapi.auth.jwt.TokenProvider;
import org.example.tamaapi.repository.MemberAddressRepository;
import org.example.tamaapi.repository.MemberRepository;
import org.example.tamaapi.service.CacheService;
import org.example.tamaapi.service.MemberService;
import org.example.tamaapi.util.ErrorMessageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
// 테스트 끝나면 롤백 (auto_increment는 롤백 안됨)
@Transactional
class MemberApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberAddressRepository memberAddressRepository;

    @Autowired
    private MemberService memberService;

    private String EMAIL = "before_each@tama.com";
    private String PASSWORD = "test_password";

    private String accessToken;

    @BeforeEach
    void saveMember() throws Exception {
        String phone = "01011111114";
        String nickname = "sign_up_test";
        String encodedPassword = bCryptPasswordEncoder.encode(PASSWORD);

        String authString = "123456";
        cacheService.save(MyCacheType.SIGN_UP_AUTH_STRING, EMAIL, authString);

        Member member = Member.builder()
                .email(EMAIL).password(encodedPassword).phone(phone).nickname(nickname)
                .authority(Authority.MEMBER).provider(Provider.LOCAL)
                .build();

        memberRepository.save(member);

        memberRepository.findByEmail(EMAIL)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessageUtil.NOT_FOUND_MEMBER));

        accessToken = tokenProvider.generateToken(member);

        // 배송지 추가
        String addressName = "판교시장";
        String receiverNickname = member.getNickname();
        String receiverPhone = member.getPhone();
        // 우편번호
        String zipCode = "13609";
        // 도로명 주소
        String streetAddress = "경기도 성남시 분당구 내정로113번길 4 (정자동, 판교시장)";
        // 상세 주소
        String detailAddress = "판교시장";

        memberService.saveMemberAddress(member.getId(), addressName, receiverNickname, receiverPhone, zipCode, streetAddress, detailAddress);
    }

    @Test
    @DisplayName("회원가입_성공")
    void signUp() throws Exception {
        // given
        String email = "sign_up_test@tama.com";
        String phone = "01011111115";
        String password = "test_password";
        String nickname = "sign_up_test";

        String authString = "123456";
        cacheService.save(MyCacheType.SIGN_UP_AUTH_STRING, email, authString);

        SignUpMemberRequest request = new SignUpMemberRequest(email,phone,password,authString,nickname);

        // when
        mockMvc.perform(post("/api/member/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("회원가입 성공"));

        // then
        memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessageUtil.NOT_FOUND_MEMBER));
    }

    @Test
    @DisplayName("로그인_성공")
    void login() throws Exception {
        // given
        LoginRequest request = new LoginRequest(EMAIL,PASSWORD);

        //when & then
        String JWT_PATTERN = "^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_.+/=]*$";

        mockMvc.perform(post("/api/member/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", matchesPattern(JWT_PATTERN)));
    }

    @Test
    @DisplayName("회원정보_수정_성공")
    void updateMemberInformation() throws Exception {
        //given
        UpdateMemberInformationRequest request = new UpdateMemberInformationRequest(180,70);

        Member member = memberRepository.findByEmail(EMAIL)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessageUtil.NOT_FOUND_MEMBER));

        //when
        mockMvc.perform(put("/api/member/information")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("개인정보 업데이트 성공"));

        //then
        assertThat(member.getHeight()).isEqualTo(180);
        assertThat(member.getWeight()).isEqualTo(70);
    }

    @Test
    @DisplayName("배송지_저장_성공")
    void saveMemberAddress() throws Exception {
        // given
        Member member = memberRepository.findByEmail(EMAIL)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessageUtil.NOT_FOUND_MEMBER));

        String addressName = "마을회관";

        String receiverNickname = member.getNickname();

        String receiverPhone = member.getPhone();

        // 우편번호
        String zipCode = "25435";

        // 도로명 주소
        String streetAddress = "강원특별자치도 강릉시 사천면 중앙동로 71 (판교리, 판교리마을회관)";

        // 상세 주소
        String detailAddress = "마을회관";

        SaveMemberAddressRequest request = new SaveMemberAddressRequest(addressName, receiverNickname, receiverPhone, zipCode, streetAddress, detailAddress);

        // when
        mockMvc.perform(post("/api/member/address")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("배송지 저장 성공"));

        // then
        MemberAddress memberAddress = memberAddressRepository.findByMemberIdAndIsDefault(member.getId(), false)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessageUtil.NOT_FOUND_ADDRESS));

        assertThat(memberAddress.getName()).isEqualTo(addressName);
        assertThat(memberAddress.getReceiverNickName()).isEqualTo(receiverNickname);
        assertThat(memberAddress.getReceiverPhone()).isEqualTo(receiverPhone);
        assertThat(memberAddress.getZipCode()).isEqualTo(zipCode);
        assertThat(memberAddress.getStreet()).isEqualTo(streetAddress);
        assertThat(memberAddress.getDetail()).isEqualTo(detailAddress);
    }

    @Test
    @DisplayName("기본_배송지_변경_성공")
    void updateMemberAddress() throws Exception {
        // given
        Member member = memberRepository.findByEmail(EMAIL)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessageUtil.NOT_FOUND_MEMBER));
        String addressName = "마을회관";
        String receiverNickname = member.getNickname();
        String receiverPhone = member.getPhone();
        // 우편번호
        String zipCode = "25435";
        // 도로명 주소
        String streetAddress = "강원특별자치도 강릉시 사천면 중앙동로 71 (판교리, 판교리마을회관)";
        // 상세 주소
        String detailAddress = "마을회관";
        memberService.saveMemberAddress(member.getId(), addressName, receiverNickname, receiverPhone, zipCode, streetAddress, detailAddress);

        MemberAddress memberAddress = memberAddressRepository.findByMemberIdAndIsDefault(member.getId(), false)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessageUtil.NOT_FOUND_ADDRESS));

        //when
        UpdateMemberDefaultAddressRequest request = new UpdateMemberDefaultAddressRequest(memberAddress.getId());
        mockMvc.perform(put("/api/member/address/default")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("기본 배송지 변경 성공"));

        //then
        assertThat(memberAddress.isDefault()).isEqualTo(true);
    }
}