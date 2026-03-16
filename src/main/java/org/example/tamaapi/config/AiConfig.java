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
    //ai 외 일반 api는 원래 db인 mysql 쓰게하려고
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
    //메서드 이름을 vectorStore로 지으려면 기본 설정을 바꿔야함 -> 대신 myVectorStore + @Primary
    public VectorStore myVectorStore(@Qualifier("vectorJdbcTemplate") JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .vectorTableName(VECTOR_TABLE)
                .build();
    }
}