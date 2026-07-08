package com.example.aidemo;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/agent")
public class AgentController {

    private final GeminiService service;

    public AgentController(GeminiService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> ask(@Valid @RequestBody PromptRequest request) {
        String answer = service.askGemini(request.prompt());
        return ResponseEntity.ok(Map.of("response", answer));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleError(RuntimeException e) {
        return ResponseEntity.status(503).body(Map.of("error", e.getMessage()));
    }
}