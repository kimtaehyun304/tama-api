package org.example.tamaapi.service;

import jakarta.mail.internet.MimeMessage;
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

        final MimeMessagePreparator mimeMessagePreparator = new MimeMessagePreparator() {
            @Override
            public void prepare(MimeMessage mimeMessage) throws Exception {
                final MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                mimeMessageHelper.setTo(toMailAddr);
                mimeMessageHelper.setSubject("[TAMA] 회원가입 인증문자 안내");
                mimeMessageHelper.setText("인증문자 : " +authString +
                        "<p>본 메일이 생성된 이유는 해당 메일로 인증하려는 시도가 있었기 때문입니다.</p>" , true);
            }

        };
        javaMailSender.send(mimeMessagePreparator);

    }
}
