package org.example.tamaapi.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.tamaapi.domain.CustomerSupportFaq;
import org.example.tamaapi.dto.responseDto.CustomerSupportFaqResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FileLoader {

    @Value("classpath:filter-prompt.txt")
    private Resource filterPrompt;

    @Value("classpath:customer-support-faq.json")
    private Resource customerSupportFaq;

    private final ObjectMapper objectMapper;

    public String loadFilterPrompt() {
        try {
            return new String(filterPrompt.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<CustomerSupportFaq> loadCustomerSupportFaqs() {
        try {
            return objectMapper.readValue(customerSupportFaq.getInputStream(), new TypeReference<>() {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}