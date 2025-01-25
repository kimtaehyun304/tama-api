package org.example.tamaapi.config.oauth2;


import lombok.RequiredArgsConstructor;
import org.example.tamaapi.domain.Member;
import org.example.tamaapi.repository.MemberRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuth2UserCustomService extends DefaultOAuth2UserService {
    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 요청을 바탕으로 유저 정보를 담은 객체 반환
        OAuth2User user = super.loadUser(userRequest);
        saveOrUpdate(user);
        return user;
    }

    // 유저가 있으면 업데이트, 없으면 생성
    private void saveOrUpdate(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        //회원가입할때 있으면 실패해야함. 로그인일때 있으면 업데이트하고

        //memberRepository.findByEmail(email).ifPresentOrElse(member -> member.changeNickname(name), () -> memberRepository.save(Member.builder().email(email).nickname(name).build()));
        //소셜 계정 일반 계정 중복 가능성 -> 공통 예외 처리
        memberRepository.findByEmail(email).orElseGet(() -> memberRepository.save(Member.builder().email(email).nickname(name).build()));
    }
}