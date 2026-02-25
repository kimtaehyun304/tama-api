package org.example.tamaapi.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        //기본값 gpt 4 mini, temperature 0.7
        //System.out.println(chatModel);
        return ChatClient.create(chatModel);
    }
}
