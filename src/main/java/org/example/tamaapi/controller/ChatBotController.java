package org.example.tamaapi.controller;

import lombok.RequiredArgsConstructor;
import org.example.tamaapi.dto.RecommendedSqlCondition;
import org.example.tamaapi.dto.requestDto.chatbot.FaqBotRequest;
import org.example.tamaapi.dto.requestDto.chatbot.RecommendBotRequest;
import org.example.tamaapi.dto.responseDto.FaqBotResponse;
import org.example.tamaapi.dto.responseDto.RecommendResponse;
import org.example.tamaapi.dto.responseDto.SimpleResponse;
import org.example.tamaapi.repository.item.query.ItemQueryRepository;
import org.example.tamaapi.repository.item.query.dto.RecommendedItemQueryResponse;
import org.example.tamaapi.service.VectorService;
import org.example.tamaapi.util.FileLoader;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatBotController {

    private final ChatClient chatClient;
    private final FileLoader fileLoader;
    private final ItemQueryRepository itemQueryRepository;
    private final VectorService vectorService;

    //챗봇을 통한 상품 검색 (검색 필터 제공하지만 이게 더 편함)
    @PostMapping("/api/chatbot/recommend")
    public RecommendResponse recommendBot(@RequestBody RecommendBotRequest recommendBotRequest) {
        //사용자가 ~~한 상품을 추천해달라고하면 gpt가 유사어를 json으로 반환
        RecommendedSqlCondition sqlCondition = chatClient.prompt(fileLoader.loadFilterPrompt())
                .user(recommendBotRequest.getUserInputPrompt())
                .call()
                .entity(RecommendedSqlCondition.class);
        List<RecommendedItemQueryResponse> recommendedItems = itemQueryRepository.findRecommendedItem(sqlCondition);
        return new RecommendResponse(sqlCondition, recommendedItems);
    }

    @PostMapping("/api/chatbot/faq")
    public FaqBotResponse faqBot(@RequestBody FaqBotRequest faqBotRequest) {
        String answer = vectorService.searchSimilarAnswer(faqBotRequest.getUserInputPrompt());
        return new FaqBotResponse(answer);
    }

    /*
    @PostMapping("/api/chatbot/faq")
    public CustomerSupportFaqResponse faqBot(@RequestBody RecommendRequest recommendRequest) {
        //사용자가 ~~한 상품을 추천해달라고하면 gpt가 이를 분석해서 where절에 쓸 조건을 json으로 반환
        RecommendedSqlCondition sqlCondition = chatClient.prompt(fileLoader.loadFilterPrompt())
                .user(recommendRequest.getUserInputPrompt())
                .call()
                .entity(RecommendedSqlCondition.class);
        List<RecommendedItemQueryResponse> recommendedItems = itemQueryRepository.findRecommendedItem(sqlCondition);
        return new CustomerSupportFaqResponse(sqlCondition, recommendedItems);
    }
    */
}
