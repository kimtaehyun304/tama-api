package org.example.tamaapi.auth.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    @Value("${frontend.url}")
    private String FRONTEND_URL;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_CONFLICT);
        response.setContentType("text/html; charset=UTF-8");  // 문자 인코딩을 UTF-8로 설정
        String script = String.format("<script>alert('%s'); window.location.href='%s';</script>", exception.getMessage(), FRONTEND_URL);
        response.getWriter().write(script);

    }
}
