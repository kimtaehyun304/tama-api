package org.example.tamaapi.dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.tamaapi.domain.CustomerSupportFaq;

@Getter
@AllArgsConstructor
public class CustomerSupportFaqResponse {

    private String question;
    private String answer;
    private String category;

    public CustomerSupportFaqResponse(CustomerSupportFaq customerSupportFaq){
        question = customerSupportFaq.getQuestion();
        answer = customerSupportFaq.getAnswer();
        category = customerSupportFaq.getCategory();
    }

}
