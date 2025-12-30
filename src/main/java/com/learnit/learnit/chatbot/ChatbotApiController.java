package com.learnit.learnit.chatbot;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotApiController {

    private final ChatbotService chatbotService;

    @PostMapping("/chat")
    public Mono<ChatResponseDTO> chat(@RequestBody ChatRequestDTO req) {
        return chatbotService
                .chatAgentChat(req.message(), req.sessionId())
                .map(r -> new ChatResponseDTO(r.reply(), r.sessionId()));
    }
}