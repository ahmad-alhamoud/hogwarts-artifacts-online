package com.ahmad.hogwartsartifactsonline.client.ai.chat;

import com.ahmad.hogwartsartifactsonline.client.ai.chat.dto.ChatRequest;
import com.ahmad.hogwartsartifactsonline.client.ai.chat.dto.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenAiChatClient implements ChatClient {

    private final RestClient restClient;

    public OpenAiChatClient(@Value("${ai.openai.endpoint}") String endpoint,
                            @Value("${ai.openai.api-key}") String apiKey,
                            RestClient.Builder restClientBuilder
    ) {
        this.restClient = restClientBuilder
                .baseUrl(endpoint)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    @Override
    public ChatResponse generate(ChatRequest chatRequest) {
        return restClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(chatRequest)
                .retrieve()
                .body(ChatResponse.class);
    }
}
