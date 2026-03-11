package org.example.tamaapi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "spring.datasource.postgresql")
public class PostgresqlProperty {

    private String jdbcUrl;
    private String username;
    private String password;
    private String driverClassName;
}
