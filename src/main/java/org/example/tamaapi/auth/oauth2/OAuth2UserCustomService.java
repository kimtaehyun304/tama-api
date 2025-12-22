package org.example.tamaapi.auth.oauth2;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.domain.user.Authority;
import org.example.tamaapi.domain.user.Member;
import org.example.tamaapi.domain.user.Provider;
import org.example.tamaapi.repository.MemberRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2UserCustomService extends DefaultOAuth2UserService {
    private final MemberRepository memberRepository;

    //구글에 요청
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 요청을 바탕으로 유저 정보를 담은 객체 반환
        OAuth2User user = super.loadUser(userRequest);
        signUpOrLogin(user);
        return user;
    }

    private void signUpOrLogin(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        //소셜 계정 일반 계정 중복 가능성 -> 공통 예외 처리
        //회원가입할때 있으면 실패해야함. 로그인일때 있으면 업데이트하고
        //memberRepository.findByEmail(email).ifPresentOrElse(member -> member.changeNickname(name), () -> memberRepository.save(Member.builder().email(email).nickname(name).build()));
        try {
            memberRepository.findByEmail(email)
                    .ifPresentOrElse(member -> {
                                switch (member.getProvider()) {
                                    case LOCAL -> {
                                        String message = "이미 가입한 일반 계정이 있습니다.";
                                        //OAuth2AuthenticationException로 해야 OAuth2FailureHandler가 예외 처리 가능
                                        throw new OAuth2AuthenticationException(new OAuth2Error("CONFLICT"), message);
                                    }
                                    case GOOGLE -> {
                                    } // 아무 작업 X (계정이 있으면 로그인되게)
                                    //default -> throw new OAuth2AuthenticationException("지원되지 않는 Provider 유형입니다.");
                                }
                            }, () -> saveOauth2Member(email, name)
                    );
        } catch (Exception e) {
            String message = "";
            if (e instanceof DataIntegrityViolationException)
                message = "회원가입 실패";
            else
                message = "에러 발생";

            log.error("oauth2 로그인 또는 회원가입 에러. 이유={}", e.getMessage());
            //OAuth2AuthenticationException로 해야 OAuth2FailureHandler가 예외 처리 가능
            throw new OAuth2AuthenticationException(new OAuth2Error("CONFLICT"), message);
        }
    }

    public void saveOauth2Member(String email, String name) {
        memberRepository.save(Member.builder()
                .provider(Provider.GOOGLE)
                .authority(Authority.MEMBER)
                .email(email)
                .nickname(name)
                .build());
    }

}