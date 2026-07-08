package com.example.aidemo;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final GeminiService geminiService;
    private final ConversationStore conversationStore;

    public ChatController(GeminiService geminiService, ConversationStore conversationStore) {
        this.geminiService = geminiService;
        this.conversationStore = conversationStore;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> chat(@Valid @RequestBody ChatRequest request) {
        String sessionId = request.sessionId();
        List<ChatTurn> history = conversationStore.getHistory(sessionId);

        String answer = geminiService.askGeminiWithHistory(history, request.prompt(), request.style());

        conversationStore.addTurn(sessionId, "user", request.prompt());
        conversationStore.addTurn(sessionId, "model", answer);

        return ResponseEntity.ok(Map.of("response", answer, "sessionId", sessionId));
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> clearSession(@PathVariable String sessionId) {
        conversationStore.clear(sessionId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleError(RuntimeException e) {
        return ResponseEntity.status(503).body(Map.of("error", e.getMessage()));
    }
}