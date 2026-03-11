package org.example.tamaapi.dto.requestDto.chatbot;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RecommendBotRequest {

    @NotNull
    private String userInputPrompt;

}

