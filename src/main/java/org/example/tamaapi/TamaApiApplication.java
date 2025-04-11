package org.example.tamaapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class TamaApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TamaApiApplication.class, args);
    }

}
