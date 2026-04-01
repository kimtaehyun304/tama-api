package org.example.tamaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;
    private ThreadPoolTaskExecutor taskExecutor;

    @Async("emailExecutor")
    @Retryable(backoff = @Backoff(delay = 3000, multiplier = 2), recover = "recover")
    //aop라 public
    public void sendSignedUpEmail(String toMailAddr) {
        String subject = "[TAMA] 회원가입 완료 안내";
        String body = String.format("<p>TAMA 쇼핑몰에 오신 것을 환영합니다</p>");
        sendEmail(toMailAddr, subject, body);
    }

    @Async("emailExecutor")
    @Retryable(backoff = @Backoff(delay = 3000, multiplier = 2), recover = "recover")
    public void sendAuthenticationEmail(String toMailAddr, String authString) {
        String subject = "[TAMA] 회원가입 인증문자 안내";
        String body = String.format("인증문자 : %s <p>본 메일이 생성된 이유는 해당 메일로 인증하려는 시도가 있었기 때문입니다.</p>", authString);
        sendEmail(toMailAddr, subject, body);
    }

    @Async("emailExecutor")
    @Retryable(backoff = @Backoff(delay = 3000, multiplier = 2), recover = "recover")
    public void sendGuestOrderEmailAsync(String toMailAddr, String buyerName, Long orderId) {
        String subject = "[TAMA] 비회원 주문 결제 안내";
        String body = String.format("주문자 이름: %s, 주문 번호: %s <p>TAMA 사이트에서 주문 상세정보를 볼 수 있습니다.</p>", buyerName, orderId);
        sendEmail(toMailAddr, subject, body);
    }

    private void sendEmail(String toMailAddr, String subject, String body) {
        MimeMessagePreparator mimeMessagePreparator = createMimeMessagePreparator(toMailAddr, subject, body);
        javaMailSender.send(mimeMessagePreparator);
    }

    private MimeMessagePreparator createMimeMessagePreparator(String toMailAddr, String subject, String body) {
        return mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(toMailAddr);
            helper.setSubject(subject);
            helper.setText(body, true);
        };
    }

    @Recover
    public void recover(Exception e, String toMailAddr) {
        log.error("[회원가입 이메일 retry 실패] toMailAddr={}, error={}", toMailAddr, e.getMessage());
    }

    @Recover
    //파라미터 안 필요해도 Retryable 메서드 파라미터랑 일치 해야 동작
    public void recover(Exception e, String toMailAddr, String authString) {
        log.error("[인증 이메일 retry 실패] toMailAddr={}, error={}", toMailAddr, e.getMessage());
    }

    @Recover
    public void recover(Exception e, String toMailAddr, String buyerName, Long orderId) {
        log.error("[비회원 주문 이메일 retry 실패] toMailAddr={}, buyerName={}, orderId={}, error={}",
                toMailAddr, buyerName, orderId, e.getMessage());
    }


}
