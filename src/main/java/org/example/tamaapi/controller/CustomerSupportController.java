package org.example.tamaapi.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.domain.CustomerSupport;
import org.example.tamaapi.domain.CustomerSupportFaq;
import org.example.tamaapi.dto.requestDto.CustomPageRequest;
import org.example.tamaapi.dto.responseDto.CustomPage;
import org.example.tamaapi.dto.responseDto.CustomerSupportFaqResponse;
import org.example.tamaapi.repository.CustomerSupportFaqRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class CustomerSupportController {

    private final CustomerSupportFaqRepository customerSupportFaqRepository;

    @GetMapping("/api/customer-support/faq")
    public CustomPage<CustomerSupportFaqResponse> faqList(@RequestParam String category){
        PageRequest pageRequest = new CustomPageRequest(1, 10).convertPageRequest();
        Page<CustomerSupportFaq> customerSupports = customerSupportFaqRepository.findAllByCategory(pageRequest, category);
        List<CustomerSupportFaqResponse> content = customerSupports.getContent().stream().map(CustomerSupportFaqResponse::new).toList();

        Pageable pageable = customerSupports.getPageable();
        int totalPages = customerSupports.getTotalPages();
        long totalElements = customerSupports.getTotalElements();

        return new CustomPage<>(content,pageable,totalPages,totalElements);
    }


}
