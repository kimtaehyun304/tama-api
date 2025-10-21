package org.example.tamaapi.event;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.domain.user.Member;
import org.example.tamaapi.domain.user.coupon.Coupon;
import org.example.tamaapi.domain.user.coupon.CouponType;
import org.example.tamaapi.domain.user.coupon.MemberCoupon;
import org.example.tamaapi.exception.MyInternalServerException;
import org.example.tamaapi.repository.CouponRepository;
import org.example.tamaapi.repository.MemberCouponRepository;
import org.example.tamaapi.repository.MemberRepository;
import org.example.tamaapi.service.EmailService;
import org.example.tamaapi.service.MemberService;
import org.example.tamaapi.service.OrderService;
import org.example.tamaapi.util.ErrorMessageUtil;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class SignedUpEventHandler {

    private final MemberRepository memberRepository;
    private final EmailService emailService;
    private final MemberService memberService;

    @EventListener
    @Async
    public void sendEmail(SignedUpEvent event) {
        //종속성 분리를 위하여, 조회하여 이메일 가져옴
        Member member = memberRepository.findById(event.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessageUtil.NOT_FOUND_MEMBER));
        if (!StringUtils.hasText(member.getEmail())) {
            String msg = "등록된 이메일이 아닙니다";
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        emailService.sendSignedUpEmail(member.getEmail());
    }

    @EventListener
    //동기 이벤트라 예외 던지면 공통 예외 처리 가능
    public void giveWelcomeCoupon(SignedUpEvent event) {
        memberService.giveWelcomeCoupon(event.getMemberId());
    }
}
