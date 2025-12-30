package com.learnit.learnit.chatbot;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChatAgentChatResponseDTO(
        @JsonProperty("session_id") String sessionId,
        String reply) {

}