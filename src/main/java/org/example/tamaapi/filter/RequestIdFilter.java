package org.example.tamaapi.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;
import java.util.UUID;

@Component
public class RequestIdFilter extends OncePerRequestFilter {

    private static final String REQ_ID_HEADER = "X-Request-Id";
    private static final String MDC_REQ_ID = "reqId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        String reqId = request.getHeader(REQ_ID_HEADER);
        if (reqId == null || reqId.isBlank())
            reqId = UUID.randomUUID().toString();

        try {
            MDC.put(MDC_REQ_ID, reqId);

            // 만약 인증에서 memberId를 얻을 수 있다면 MDC에 넣어두면 편리합니다.
            // 예: Long memberId = getMemberIdFromToken(request);
            // if (memberId != null) MDC.put(MDC_MEMBER_ID, String.valueOf(memberId));

            // 응답에도 헤더로 붙여주면 추적이 용이
            response.setHeader(REQ_ID_HEADER, reqId);

            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_REQ_ID);
        }
    }

}