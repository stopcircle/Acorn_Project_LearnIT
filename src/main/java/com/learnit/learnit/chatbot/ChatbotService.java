package com.learnit.learnit.chatbot;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final WebClient webClient;

    @Value("${chat.agent.base-url}")
    private String chatAgentBaseUrl;

    public Mono<ChatAgentChatResponseDTO> chatAgentChat(String message, String sessionId) {
        return webClient.post()
                .uri(chatAgentBaseUrl + "/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ChatAgentChatRequestDTO(message, sessionId))
                .retrieve()
                .bodyToMono(ChatAgentChatResponseDTO.class);
    }
}