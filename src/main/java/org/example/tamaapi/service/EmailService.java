package org.example.tamaapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    public void sendAuthenticationEmail(String toMailAddr, String authString) {
        String subject = "[TAMA] 회원가입 인증문자 안내";
        String body = String.format("인증문자 : %s <p>본 메일이 생성된 이유는 해당 메일로 인증하려는 시도가 있었기 때문입니다.</p>", authString);
        sendEmail(toMailAddr, subject, body);
    }

    public void sendGuestOrderEmail(String toMailAddr, String buyerName, Long orderId) {
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
}
