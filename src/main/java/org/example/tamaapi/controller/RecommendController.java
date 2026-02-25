package org.example.tamaapi.controller;

import lombok.RequiredArgsConstructor;
import org.example.tamaapi.aspect.LogExecutionTime;
import org.example.tamaapi.dto.RecommendedSqlCondition;
import org.example.tamaapi.dto.requestDto.item.RecommendRequest;
import org.example.tamaapi.dto.responseDto.RecommendResponse;
import org.example.tamaapi.repository.item.query.ItemQueryRepository;
import org.example.tamaapi.repository.item.query.dto.RecommendedItemQueryResponse;
import org.example.tamaapi.util.PromptLoader;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RecommendController {

    private final ChatClient chatClient;
    private final PromptLoader promptLoader;
    private final ItemQueryRepository itemQueryRepository;

    //gpt를 통한 상품 검색 (문장을 통한 편한 검색 + 유사어 인지 가능한 장점)
    @PostMapping("/api/ai/recommend")
    public RecommendResponse aiRecommend(@RequestBody RecommendRequest recommendRequest) {
        //사용자가 ~~한 상품을 추천해달라고하면 gpt가 이를 분석해서 where절에 쓸 조건을 json으로 반환
        RecommendedSqlCondition sqlCondition = chatClient.prompt(promptLoader.load())
                .user(recommendRequest.getUserInputPrompt())
                .call()
                .entity(RecommendedSqlCondition.class);
        List<RecommendedItemQueryResponse> recommendedItems = itemQueryRepository.findRecommendedItem(sqlCondition);
        return new RecommendResponse(sqlCondition, recommendedItems);
    }

}
