package org.example.tamaapi.service;

import lombok.RequiredArgsConstructor;
import org.example.tamaapi.domain.CustomerSupportFaq;
import org.example.tamaapi.util.FileLoader;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VectorService {

    private final VectorStore vectorStore;

    public String searchSimilarAnswer(String question) {
        //0.3은 해야 "ㅋㅋ" 이런거 쳤을때 검색결과 없다고 함
        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(1)
                        .similarityThreshold(0.35)
                        .build()
        );
        System.out.println("documents.isEmpty() = " + documents.isEmpty());
        if(documents.isEmpty()) return "";

        return documents.get(0).getMetadata().get("answer").toString();
    }

    public void saveFaqVectors(List<CustomerSupportFaq> faqs) {
        List<Document> docs = new ArrayList<>();

        for (CustomerSupportFaq faq : faqs) {
            String replacedAnswer = faq.getAnswer().replaceAll("\n", " ");

            Document doc = new Document(
                    String.format("Q: %s A: %s", faq.getQuestion(), replacedAnswer),
                    Map.of(
                            "answer", faq.getAnswer(),
                            "category", faq.getCategory()
                    )
            );
            docs.add(doc);
        }

        vectorStore.add(docs);
    }

}
