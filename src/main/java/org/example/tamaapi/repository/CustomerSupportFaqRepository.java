package org.example.tamaapi.repository;

import org.example.tamaapi.domain.CustomerSupport;
import org.example.tamaapi.domain.CustomerSupportFaq;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerSupportFaqRepository extends JpaRepository<CustomerSupportFaq, Long> {

    Page<CustomerSupportFaq> findAllByCategory(Pageable pageable, String category);
}
