package org.example.tamaapi.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class AiConfig {

    @Autowired
    private PostgresqlProperty postgresqlProperty;

    private final String VECTOR_SCHEMA = "tama_vector";
    private final String VECTOR_TABLE = "faq_vector";

    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        //기본값 gpt 4 mini, temperature 0.7
        //System.out.println(chatModel);
        return ChatClient.create(chatModel);
    }

    @Bean
    @Primary
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "vectorJdbcTemplate")
    public JdbcTemplate vectorJdbcTemplate() {
        HikariDataSource dataSource = DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .url(postgresqlProperty.getJdbcUrl())
                .username(postgresqlProperty.getUsername())
                .password(postgresqlProperty.getPassword())
                .driverClassName(postgresqlProperty.getDriverClassName())
                .build();
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @Primary
    //vectorStore로 메서드 이름 지으면 기본 설정(overrride) 바꿔야 해서 이렇게
    public VectorStore myVectorStore(@Qualifier("vectorJdbcTemplate") JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .vectorTableName(VECTOR_TABLE)
                .build();
    }
}